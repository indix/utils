package com.indix.gocd.utils

object Constants {
  val METADATA_USER = "user"
  val METADATA_TRACEBACK_URL = "traceback_url"
  val COMPLETED = "completed"

  val GO_ARTIFACTS_S3_BUCKET = "GO_ARTIFACTS_S3_BUCKET"
  val GO_SERVER_DASHBOARD_URL = "GO_SERVER_DASHBOARD_URL"

  val SOURCEDESTINATIONS = "sourceDestinations"
  val DESTINATION_PREFIX = "destinationPrefix"
  val ARTIFACTS_BUCKET = "artifactsBucket"

  val AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY"
  val AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID"
  val AWS_REGION = "AWS_REGION"
  val AWS_USE_IAM_ROLE = "AWS_USE_IAM_ROLE"
  val AWS_STORAGE_CLASS = "AWS_STORAGE_CLASS"
  val STORAGE_CLASS_STANDARD = "standard"
  val STORAGE_CLASS_STANDARD_IA = "standard-ia"
  val STORAGE_CLASS_RRS = "rrs"
  val STORAGE_CLASS_GLACIER = "glacier"

  val GO_PIPELINE_LABEL = "GO_PIPELINE_LABEL"

  val MATERIAL_TYPE = "MaterialType"
  val REPO = "Repo"
  val PACKAGE = "Package"
  val MATERIAL = "Material"
  val JOB = "Job"
  val STAGE = "Stage"
  val SOURCE = "Source"
  val SOURCE_PREFIX = "SourcePrefix"
  val DESTINATION = "Destination"

  val REQUIRED_FIELD_MESSAGE = "This field is required"
}
