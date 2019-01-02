package com.indix.gocd.utils.store

import java.io.File

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ListObjectsRequest, ObjectListing, PutObjectRequest}
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.mockito.{ArgumentCaptor, Mock, Mockito}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.collection.JavaConversions._

class S3ArtifactStoreSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  @Mock
  private val mockClient = MockitoSugar.mock[AmazonS3Client]
  private val putCaptor = ArgumentCaptor.forClass(classOf[PutObjectRequest])
  private val listingCaptor: ArgumentCaptor[ListObjectsRequest] = ArgumentCaptor.forClass(classOf[ListObjectsRequest])
  private val store = new S3ArtifactStore(mockClient, "foo-bar")

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(mockClient)
  }

  "put" should "use standard storage class as default" in {
    val store = new S3ArtifactStore(mockClient, "foo-bar")
    store.put(new PutObjectRequest("foo-bar", "key", new File("/tmp/baz")))
    verify(mockClient, times(1)).putObject(putCaptor.capture)

    val putRequest = putCaptor.getValue
    putRequest.getStorageClass should be("STANDARD")
  }

  it should "use standard-ia storage class as default" in {
    val store = new S3ArtifactStore(mockClient, "foo-bar").withStorageClass("standard-ia")
    store.put(new PutObjectRequest("foo-bar", "key", new File("/tmp/baz")))
    verify(mockClient, times(1)).putObject(putCaptor.capture)

    val putRequest = putCaptor.getValue
    putRequest.getStorageClass should be("STANDARD_IA")
  }

  it should "use reduced redundancy class as default" in {
    val store = new S3ArtifactStore(mockClient, "foo-bar").withStorageClass("rrs")
    store.put(new PutObjectRequest("foo-bar", "key", new File("/tmp/baz")))
    verify(mockClient, times(1)).putObject(putCaptor.capture)

    val putRequest = putCaptor.getValue
    putRequest.getStorageClass should be("REDUCED_REDUNDANCY")
  }

  it should "use glacier storage class as default" in {
    val store = new S3ArtifactStore(mockClient, "foo-bar").withStorageClass("glacier")
    store.put(new PutObjectRequest("foo-bar", "key", new File("/tmp/baz")))
    verify(mockClient, times(1)).putObject(putCaptor.capture)

    val putRequest = putCaptor.getValue
    putRequest.getStorageClass should be("GLACIER")
  }

  "bucketExists" should "successfully check if bucket exists" in {
    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenReturn(new ObjectListing)
    store.bucketExists should be(true)
  }

  it should "return false when bucket does not exist" in {
    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenThrow(new RuntimeException("Bucket does not exist"))
    try {
      store.bucketExists
    }
    catch {
      case e: RuntimeException => e.getMessage should be("Bucket does not exist")
    }
  }

  "listObjects" should "return the right objects list" in {
    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenReturn(null)
    store.getLatestPrefix("pipeline", "stage", "job", "1")
    verify(mockClient).listObjects(listingCaptor.capture())

    val request = listingCaptor.getValue
    request.getBucketName should be("foo-bar")
    request.getPrefix should be("pipeline/stage/job/1.")
    request.getDelimiter should be("/")
  }

  "getLatestPrefix" should "not be defined when object listing is null" in {
    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenReturn(null)
    val latestPrefix = store.getLatestPrefix("pipeline", "stage", "job", "1")

    latestPrefix.isDefined should be(false)
  }

  it should "not be defined when object listing size is 0" in {
    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenReturn(new ObjectListing)
    val latestPrefix = store.getLatestPrefix("pipeline", "stage", "job", "1")

    latestPrefix.isDefined should be(false)
  }

  it should "return the latest stage counter from the listing" in {
    val listing = new ObjectListing
    listing.setCommonPrefixes(List("pipeline/stage/job/1.2", "pipeline/stage/job/1.1", "pipeline/stage/job/1.7"))

    when(mockClient.listObjects(any(classOf[ListObjectsRequest]))).thenReturn(listing)
    val latestPrefix = store.getLatestPrefix("pipeline", "stage", "job", "1")

    latestPrefix.isDefined should be(true)
    latestPrefix.get should be("pipeline/stage/job/1.7")
  }
}
