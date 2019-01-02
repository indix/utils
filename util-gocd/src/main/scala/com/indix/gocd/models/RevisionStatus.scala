package com.indix.gocd.models

import java.text.SimpleDateFormat
import java.util.Date

import com.amazonaws.util.StringUtils

case class RevisionStatus(revision: Revision, lastModified: Date, trackbackUrl: String, user: String, revisionLabel: String = "") {

  val DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  def toMap: Map[String, String] = {
    Map(
      "revision" -> revision.revision,
      "timestamp" -> new SimpleDateFormat(DATE_PATTERN).format(lastModified),
      "user" -> user,
      "revisionComment" -> String.format("Original revision number: %s", if (StringUtils.isNullOrEmpty(revisionLabel)) "unavailable"
      else revisionLabel),
      "trackbackUrl" -> trackbackUrl
    )
  }
}
