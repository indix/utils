package com.indix.gocd.utils.store

import java.io.File

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials, InstanceProfileCredentialsProvider}
import com.amazonaws.services.s3.model._
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.indix.gocd.models.{Artifact, ResponseMetadataConstants, Revision, RevisionStatus}
import com.indix.gocd.utils.Constants._
import com.indix.gocd.utils.GoEnvironment
import com.indix.gocd.utils.store.S3ArtifactStore.getS3client

import scala.collection.JavaConversions._

/**
  * Lists objects, finds latest in s3 bucket
  *
  * @param client
  * @param bucket
  * @param storageClass
  */
class S3ArtifactStore(client: AmazonS3 = getS3client(new GoEnvironment()), bucket: String, storageClass: StorageClass = StorageClass.Standard) {

  def withStorageClass(storageClass: String): S3ArtifactStore = {
    val key = storageClass.toLowerCase()
    if (S3ArtifactStore.STORAGE_CLASSES.contains(key)) {
      new S3ArtifactStore(client, bucket, S3ArtifactStore.STORAGE_CLASSES(key))
    }
    else {
      throw new IllegalArgumentException("Invalid storage class specified for S3 - " + storageClass + ". Accepted values are standard, standard-ia, rrs and glacier")
    }
  }

  /**
    * Puts object request in client
    *
    * @param putObjectRequest
    * @return
    */
  def put(putObjectRequest: PutObjectRequest): PutObjectResult = {
    putObjectRequest.setStorageClass(this.storageClass)
    client.putObject(putObjectRequest)
  }

  def put(from: String, to: String, metadata: ObjectMetadata): PutObjectResult = {
    put(new PutObjectRequest(bucket, to, new File(from)).withMetadata(metadata))
  }

  def put(from: String, to: String): PutObjectResult = {
    put(new PutObjectRequest(bucket, to, new File(from)))
  }

  def pathString(pathOnS3: String): String = String.format("s3://%s/%s", bucket, pathOnS3)

  /**
    * Gets objects from path and puts in the destination
    *
    * @param from
    * @param to
    * @return
    */
  def get(from: String, to: String): ObjectMetadata = {
    val getObjectRequest = new GetObjectRequest(bucket, from)
    val destinationFile = new File(to)
    destinationFile.getParentFile.mkdirs
    client.getObject(getObjectRequest, destinationFile)
  }

  def getMetadata(key: String): ObjectMetadata = client.getObjectMetadata(bucket, key)

  def getPrefix(prefix: String, to: String): Unit = {
    val listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix)
    getAll(client.listObjects(listObjectsRequest))

    def getAll(objectListing: ObjectListing): Unit = {
      if (objectListing.isTruncated) None
      else {
        for (objectSummary <- objectListing.getObjectSummaries) {
          val destinationPath = to + "/" + objectSummary.getKey.replace(prefix + "/", "")
          if (objectSummary.getSize > 0) {
            val x = get(objectSummary.getKey, destinationPath)
          }
        }
        listObjectsRequest.setMarker(objectListing.getNextMarker)
        getAll(client.listObjects(listObjectsRequest))
      }
    }
  }

  def bucketExists: Boolean = {
    try {
      client.listObjects(new ListObjectsRequest(bucket, null, null, null, 0))
      true
    }
    catch {
      case e: Exception => false
    }
  }

  def exists(bucket: String, key: String): Boolean = {
    val listObjectRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(key).withDelimiter("/")
    try {
      val listing = client.listObjects(listObjectRequest)
      listing != null && !listing.getCommonPrefixes.isEmpty
    }
    catch {
      case e: Exception => false
    }
  }

  private def isComplete(prefix: String): Boolean = client.getObjectMetadata(bucket, prefix).getUserMetadata.contains(ResponseMetadataConstants.COMPLETED)

  private def mostRecentRevision(objectListing: ObjectListing): Revision = {
    val prefixes = objectListing.getCommonPrefixes.filter(isComplete)
    val revisions = prefixes.map(prefix => Revision(prefix.split("/").last))
    if (revisions.isEmpty) Revision.base
    else revisions.max
  }

  private def latestOfInternal(objectListing: ObjectListing, latestRevision: Revision): Revision = {
    if (!objectListing.isTruncated) latestRevision
    else {
      val objects = client.listNextBatchOfObjects(objectListing)
      val mostRecent = mostRecentRevision(objects)
      latestOfInternal(objectListing, latestRevision = {
        if (latestRevision.compareTo(mostRecent) > 0) latestRevision
        else mostRecent
      })
    }
  }

  private def latestOf(objectListing: ObjectListing) = latestOfInternal(objectListing, mostRecentRevision(objectListing))

  def getLatest(artifact: Artifact): Option[RevisionStatus] = {
    val listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(artifact.prefix).withDelimiter("/")

    val objectListing = client.listObjects(listObjectsRequest)
    if (objectListing != null) {
      val recent = latestOf(objectListing)

      val getObjectMetadataRequest = new GetObjectMetadataRequest(bucket, artifact.withRevision(recent).prefixWithRevision)
      val metadata = client.getObjectMetadata(getObjectMetadataRequest)
      val userMetadata = metadata.getUserMetadata

      val tracebackUrl = userMetadata(ResponseMetadataConstants.TRACEBACK_URL)
      val user = userMetadata(ResponseMetadataConstants.USER)
      val revisionLabel = userMetadata.getOrDefault(ResponseMetadataConstants.GO_PIPELINE_LABEL, "")

      Some(RevisionStatus(recent, metadata.getLastModified, tracebackUrl, user, revisionLabel))
    }
    else None
  }

  def getLatestPrefix(pipeline: String, stage: String, job: String, pipelineCounter: String): Option[String] = {
    val prefix = String.format("%s/%s/%s/%s.", pipeline, stage, job, pipelineCounter)

    val listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix).withDelimiter("/")
    val objectListing = client.listObjects(listObjectsRequest)

    if (objectListing != null) {
      val stageCounters = objectListing.getCommonPrefixes.map(input => input.replaceAll(prefix, "").replaceAll("/", ""))

      if (stageCounters.nonEmpty) Some(prefix + stageCounters.maxBy(counter => Integer.valueOf(counter)))
      else None
    }
    else None
  }

  def this(env: GoEnvironment, bucket: String) = this(getS3client(env), bucket, StorageClass.Standard)

  def this(client: AmazonS3, bucket: String) = this(client, bucket, StorageClass.Standard)
}

object S3ArtifactStore {
  val STORAGE_CLASSES = Map(
    STORAGE_CLASS_STANDARD -> StorageClass.Standard,
    STORAGE_CLASS_STANDARD_IA -> StorageClass.StandardInfrequentAccess,
    STORAGE_CLASS_RRS -> StorageClass.ReducedRedundancy,
    STORAGE_CLASS_GLACIER -> StorageClass.Glacier
  )

  def getS3client(env: GoEnvironment): AmazonS3 = {

    def withRegion(clientBuilder: AmazonS3ClientBuilder) = {
      if (env.has(AWS_REGION)) clientBuilder.withRegion(env.get(AWS_REGION))
      else clientBuilder
    }

    def withCredentials(clientBuilder: AmazonS3ClientBuilder) = {
      if (env.hasAWSUseIamRole) clientBuilder.withCredentials(new InstanceProfileCredentialsProvider(false))
      else if (env.has(AWS_ACCESS_KEY_ID) && env.has(AWS_SECRET_ACCESS_KEY)) {
        val basicAWSCredentials = new BasicAWSCredentials(env.get(AWS_ACCESS_KEY_ID), env.get(AWS_SECRET_ACCESS_KEY))
        clientBuilder.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
      }
      else clientBuilder
    }

    withCredentials(withRegion(AmazonS3ClientBuilder.standard())).build()
  }
}