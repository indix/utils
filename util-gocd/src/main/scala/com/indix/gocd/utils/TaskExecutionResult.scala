package com.indix.gocd.utils

import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse

case class TaskExecutionResult(success: Boolean, message: String, exception: Option[Exception] = None) {

  def toMap = Map("success" -> success, "message" -> message)

  def responseCode: Int = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE
}
