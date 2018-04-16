package com.indix.utils.core

import org.apache.commons.lang3.text.WordUtils

object MPN {
  val BlackListedMpns = Set("unknown", "none", "does not apply", "non applicable", "various")

  val Alpha = (('a' to 'z') ++ ('A' to 'Z')).toSet
  val Numbers = ('0' to '9').toSet
  val UpperCaseAlphabet = ('A' to 'Z').toSet
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
      case _ => true
    }
  }

  private def isNumeric(input: String): Boolean = {
    input.toList.forall(c => Numbers.contains(c))
  }

  def isTitleCase(str: String): Boolean = {
    val words = str.split(' ').filter(_.nonEmpty)
    if (words.length < 2) false
    else words.forall(w => w == WordUtils.capitalizeFully(w) && !isNumeric(w))
  }
}