package com.indix.utils.core

object StringUtils {

  def removeSpaces(str: String) = {
    str
      .replaceAll("\u00A0", " ")
      .replaceAll("\\s+", " ")
  }

}
