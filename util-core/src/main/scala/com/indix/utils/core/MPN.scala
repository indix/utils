package com.indix.utils.core

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.text.WordUtils

object MPN {
  // Some domain specific keywords known to be invalid
  val BlackListedMpns = Set("unknown", "none", "does not apply", "non applicable", "na", "n/a", "various")

  val StopChars = Set(' ', '-', '_', '.', '/')
  val TerminateChars = Set(',', '"', '*', '%')

  val MaxLen = 50
  val MinLen = 3

  // Check if identifier is valid, also return the identifier to process further if any
  def validateIdentifier(text: String): (Boolean, String) = {
    val input = if (text != null) text.trim() else text
    input match {
      case _ if StringUtils.isBlank(input) || input.length > MaxLen || input.length < MinLen => (false, "")
      case _ if input.count(c => TerminateChars.contains(c)) > 1 => (false, input)
      case _ if BlackListedMpns.contains(input.toLowerCase) => (false, "")
      case _ if isTitleCase(input) => (false, "")
      // Unicode strings yet to be handled
      case _ => (true, input)
    }
  }

  def isValidIdentifier(value: String): Boolean = validateIdentifier(value)._1

  // Does not consider numbers or one word strings as title-case phrase
  def isTitleCase(str: String): Boolean = {
    val words = str.split(' ').filter(_.nonEmpty)
    if (words.length < 2) false
    else words.forall(w => w == WordUtils.capitalizeFully(w) && !StringUtils.isNumeric(w))
  }

  def postProcessIdentifier(input: String): String = {
    val trimmedUpper = input.trim.toUpperCase
    trimmedUpper
  }

  def standardizeMPN(input: String): Option[String] = {
    val (isValid, identifier) = validateIdentifier(input)
    if (isValid) {
      Some(postProcessIdentifier(input))
    } else if (StringUtils.isBlank(identifier)) {
      None
    } else if (identifier.indexWhere(c => TerminateChars.contains(c)) > 0) {
      Some(postProcessIdentifier(identifier.substring(0, identifier.indexWhere(c => TerminateChars.contains(c)))))
    }
    else None
  }
}