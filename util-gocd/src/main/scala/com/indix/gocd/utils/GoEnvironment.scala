package com.indix.gocd.utils

import java.util.regex.Pattern

import com.indix.gocd.utils.Constants.{AWS_USE_IAM_ROLE, GO_SERVER_DASHBOARD_URL}
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils.isNotEmpty

import scala.collection.JavaConversions._

class GoEnvironment(input: Option[Map[String, String]] = None) {

  val environment: Map[String, String] = {
    if (input.isDefined)
      System.getenv().toMap ++ input.get
    else
      System.getenv().toMap
  }
  val envPat: Pattern = Pattern.compile("\\$\\{(\\w+)\\}")

  def putAll(existing: Map[String, String]): GoEnvironment = new GoEnvironment(Some(existing ++ environment))

  def asMap: Map[String, String] = environment

  def get(name: String): String = environment(name)

  def getOrElse(name: String, defaultValue: String): String = environment.getOrElse(name, defaultValue)

  def has(name: String): Boolean = environment.containsKey(name) && isNotEmpty(get(name))

  def isAbsent(name: String): Boolean = !has(name)

  def traceBackUrl: String = {
    val serverUrl = get(GO_SERVER_DASHBOARD_URL)
    val pipelineName = get("GO_PIPELINE_NAME")
    val pipelineCounter = get("GO_PIPELINE_COUNTER")
    val stageName = get("GO_STAGE_NAME")
    val stageCounter = get("GO_STAGE_COUNTER")
    val jobName = get("GO_JOB_NAME")
    String.format("%s/go/tab/build/detail/%s/%s/%s/%s/%s", serverUrl, pipelineName, pipelineCounter, stageName, stageCounter, jobName)
  }

  def triggeredUser: String = get("GO_TRIGGER_USER")

  def replaceVariables(str: String): String = {
    val m = envPat.matcher(str)
    val sb = new StringBuffer
    while (m.find()) {
      val replacement = environment.get(m.group(1))
      if (replacement.isDefined) {
        m.appendReplacement(sb, replacement.get)
      }
    }
    m.appendTail(sb)
    sb.toString
  }

  /**
    * Version Format on S3 is <code>pipeline/stage/job/pipeline_counter.stage_counter</code>
    */
  def artifactsLocationTemplate: String = {
    val pipeline = get("GO_PIPELINE_NAME")
    val stageName = get("GO_STAGE_NAME")
    val jobName = get("GO_JOB_NAME")
    val pipelineCounter = get("GO_PIPELINE_COUNTER")
    val stageCounter = get("GO_STAGE_COUNTER")
    artifactsLocationTemplate(pipeline, stageName, jobName, pipelineCounter, stageCounter)
  }

  def artifactsLocationTemplate(pipeline: String, stageName: String, jobName: String, pipelineCounter: String, stageCounter: String): String = String.format("%s/%s/%s/%s.%s", pipeline, stageName, jobName, pipelineCounter, stageCounter)

  private val validUseIamRoleValues: List[String] = List("true", "false", "yes", "no", "on", "off")

  def hasAWSUseIamRole: Boolean = {
    if (!has(AWS_USE_IAM_ROLE)) false
    val useIamRoleValue = get(AWS_USE_IAM_ROLE)
    val result = BooleanUtils.toBooleanObject(useIamRoleValue)
    if (result == null) throw new IllegalArgumentException(getEnvInvalidFormatMessage(AWS_USE_IAM_ROLE, useIamRoleValue, validUseIamRoleValues.mkString("[", ", ", "]")))
    else result.booleanValue
  }

  private def getEnvInvalidFormatMessage(environmentVariable: String, value: String, expected: String) = String.format("Unexpected value in %s environment variable; was %s, but expected one of the following %s", environmentVariable, value, expected)

  def this() = this(None)
}