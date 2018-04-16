package com.indix.utils.core

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils

object MPN {
  // Some domain specific keywords known to be invalid
  val BlackListedMpns = Set("unknown", "none", "does not apply", "non applicable", "various")

  val StopChars = Set(' ', '-', '_', '.', '/')
  val TerminateChars = Set(',', '"', '*', '%')

  val MaxLen = 50
  val MinLen = 3

  def isValidIdentifier(input: String): Boolean = {
    input match {
      case _ if input.length > MaxLen || input.length < MinLen => false
      case _ if input.count(c => TerminateChars.contains(c)) > 1 => false
      case _ if BlackListedMpns.contains(input.toLowerCase) => false
      case _ if isTitleCase(input) => false
      // Unicode strings yet to be handled
      case _ => true
    }
  }

  // Does not consider numbers or one word strings as title-case phrase
  def isTitleCase(str: String): Boolean = {
    val words = str.split(' ').filter(_.nonEmpty)
    if (words.length < 2) false
    else words.forall(w => w == WordUtils.capitalizeFully(w) && !StringUtils.isNumeric(w))
  }
}