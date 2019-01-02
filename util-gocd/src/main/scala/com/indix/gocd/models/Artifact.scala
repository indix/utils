package com.indix.gocd.models

/**
  * S3 Artifact
  *
  * A gocd pipeline can create an s3 artifact at a location that can
  * be identified using the pipeline details like pipeline name,
  * stage name, job name, etc
  *
  * @param pipelineName
  * @param stageName
  * @param jobName
  * @param revision
  */
case class Artifact(pipelineName: String, stageName: String, jobName: String, revision: Option[Revision] = None) {

  def withRevision(revision: Revision): Artifact = Artifact(pipelineName, stageName, jobName, Some(revision))

  def prefix: String = String.format("%s/%s/%s/", pipelineName, stageName, jobName)

  def prefixWithRevision: String = {
    if (revision.isDefined) String.format("%s/%s/%s/%s/", pipelineName, stageName, jobName, revision.get.revision)
    else prefix
  }
}
