package com.indix.gocd.utils

import com.indix.gocd.utils.Constants.GO_SERVER_DASHBOARD_URL
import org.scalatest.{FlatSpec, Matchers}

class GoEnvironmentSpec extends FlatSpec with Matchers {

  val mockEnvironmentVariables = Map(
    GO_SERVER_DASHBOARD_URL -> "http://go.server:8153",
    "GO_SERVER_URL" -> "https://localhost:8154/go",
    "GO_PIPELINE_NAME" -> "s3-publish-test",
    "GO_PIPELINE_COUNTER" -> "20",
    "GO_STAGE_NAME" -> "build-and-publish",
    "GO_STAGE_COUNTER" -> "1",
    "GO_JOB_NAME" -> "publish",
    "GO_TRIGGER_USER" -> "Krishna")

  val goEnvironment = new GoEnvironment(Some(mockEnvironmentVariables))

  "traceBackUrl" should "generate trace back url" in {
    goEnvironment.traceBackUrl should be("http://go.server:8153/go/tab/build/detail/s3-publish-test/20/build-and-publish/1/publish")
  }

  "triggeredUser" should "return triggered user" in {
    goEnvironment.triggeredUser should be("Krishna")
  }

  "artifactsLocationTemplate" should "generate artifacts location template" in {
    goEnvironment.artifactsLocationTemplate should be("s3-publish-test/build-and-publish/publish/20.1")
  }

  "asMap" should "return as map" in {
    mockEnvironmentVariables.foreach(tuple => goEnvironment.get(tuple._1) should be(tuple._2))
  }

  "replaceVariables" should "replace the env variables with respective values in the template string" in {
    val template = "COUNT:${GO_STAGE_COUNTER} Name:${GO_STAGE_NAME} COUNT2:${GO_STAGE_COUNTER}"
    val replaced = goEnvironment.replaceVariables(template)
    replaced should be("COUNT:1 Name:build-and-publish COUNT2:1")
  }

  it should "not replace unknown env variables in the template string" in {
    val template = "COUNT:${GO_STAGE_COUNTER} ${DOESNT_EXIST}"
    val replaced = goEnvironment.replaceVariables(template)
    replaced should be("COUNT:1 ${DOESNT_EXIST}")
  }

  "hasAWSUseIamRole" should "return true if it is set to true" in {
    val env = goEnvironment.putAll(mockEnvironmentVariables ++ Map(Constants.AWS_USE_IAM_ROLE -> "True"))
    env.hasAWSUseIamRole should be(true)
  }

  it should "return false if it is set to false" in {
    val env = goEnvironment.putAll(mockEnvironmentVariables ++ Map(Constants.AWS_USE_IAM_ROLE -> "False"))
    env.hasAWSUseIamRole should be(false)
  }

  it should "throw exception if the value is not a valid boolean" in {
    val env = goEnvironment.putAll(mockEnvironmentVariables ++ Map(Constants.AWS_USE_IAM_ROLE -> "Blue"))

    try {
      env.hasAWSUseIamRole
      fail("Expected Exception")
    }
    catch {
      case e: Exception => e.getMessage should be
        "Unexpected value in AWS_USE_IAM_ROLE environment variable; was Blue, but expected one of " +
          "the following [true, false, yes, no, on, off]"
    }
  }
}
