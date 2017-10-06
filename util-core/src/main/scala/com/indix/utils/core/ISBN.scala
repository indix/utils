package com.indix.utils.core

import org.apache.commons.lang3.StringUtils

case class ISBN(isbn: String, isbn10: Option[String] = None) {
  override def toString = isbn
}

object ISBN {
  def apply(input: String): Option[ISBN] = {
    Some(input)
      .filter(!StringUtils.isEmpty(_))
      .map(clean)
      .flatMap {
        case x if isValidIsbn10(x) =>
          Some(new ISBN(isbn10to13(x), Some(x)))
        case x if isValidIsbn13(x) =>
          Some(new ISBN(x))
        case _ => None
      }
  }

  private def clean(input: String) = {
    input.replaceAll("[ -]", "")
  }

  private def calculateCheckDigit13(input: String) = {
    val inputWithoutChecksum = input.dropRight(1)

    val sum = inputWithoutChecksum.zipWithIndex.map{
      case (c, i) if i % 2 != 0 => (c - '0') * 3
      case (c, _) => c - '0'
    }.sum

    (10 - (sum % 10) + '0').toChar
  }

  private def calculateCheckDigit10(input: String) = {
    val sum = input.dropRight(1).map(_ - '0').zip(10 to 2 by -1).foldLeft(0)((i: Int, tuple: (Int, Int)) => i + tuple._1 * tuple._2)
    val checkDigit = (11 - sum % 11) % 11
    if (checkDigit == 10) 'X' else (checkDigit + '0').toChar
  }

  private def isbn10to13(input: String) = {
    val withPrefix = "978" + input
    withPrefix.dropRight(1) + calculateCheckDigit13(withPrefix)
  }

  private def isValidIsbn13(input: String) = {
    input.length == 13 && input.matches("^97[89].+") && input.last == calculateCheckDigit13(input)
  }

  private def isValidIsbn10(input: String) = {
    input.length == 10 && input.last == calculateCheckDigit10(input)
  }

}
