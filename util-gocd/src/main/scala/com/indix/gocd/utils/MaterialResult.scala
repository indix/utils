package com.indix.gocd.utils

import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse

case class MaterialResult(success: Boolean, messages: Seq[String]) {

  def toMap = Map(
    "status" -> {
      if (success) "success" else "failure"
    },
    "messages" -> messages
  )

  def responseCode: Int = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE
}

object MaterialResult {

  def apply(success: Boolean) : MaterialResult = MaterialResult(success, Seq())

  def apply(success: Boolean, message: String) : MaterialResult = MaterialResult(success, Seq(message))
}