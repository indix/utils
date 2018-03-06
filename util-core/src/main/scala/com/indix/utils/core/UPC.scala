package com.indix.utils.core

import org.apache.commons.lang3.StringUtils

import scala.util.Try

object UPC {

  /**
    * Standardizes a UPC in the GTIN-14 format
    * If the given string is not a valid UPC, the method throws an `IllegalArgumentException`
    * @return - Standardized UPC
    */

  def standardize(rawUpc: String) : String = {

    def standardizeRec(input: String) : String = {
      if (input.length < 12) {
        standardizeRec(leftPadZeroes(input, 12))
      }
      else if (input.length == 12) {
        val cDigit = calculateCheckDigit(input.substring(0, 11))
        if (input.last == cDigit + '0' && !isIsbn(input)) {
          input
        } else {
          val cDigit13 = calculateCheckDigit(leftPadZeroes(input, 13))
          input + cDigit13
        }
      } else {
        input
      }
    }

    val cleanedUpc = verifyValidUpc(clean(rawUpc))

    if(isIsbn(rawUpc)) {
      leftPadZeroes(standardizeRec(cleanedUpc), 13)
    } else {
      leftPadZeroes(standardizeRec(cleanedUpc), 14)
    }


  }

  private def isIsbn(input: String) = {
    input.startsWith("978") || input.startsWith("979")
  }

  private def calculateCheckDigit(input: String) = {

    val sumOddDigits = input.zipWithIndex
      .filter { case (digit, index) => (index + 1) % 2 != 0 }
      .map { case (digit, index) => digit - '0' }
      .sum

    val sumEvenDigits = input.zipWithIndex
      .filter { case (digit, index) => (index + 1) % 2 == 0 }
      .map { case (digit, index) => digit - '0' }
      .sum

    val checkDigitSum = sumOddDigits * 3 + sumEvenDigits

    if (checkDigitSum % 10 == 0) 0 else 10 - (checkDigitSum % 10)
  }

  private def leftPadZeroes(s: String, length: Int) = StringUtils.leftPad(s, length, '0')

  private def clean(input: String) = input.replaceAll("-", "")


  private def verifyValidUpc(input: String) = {
    if (StringUtils.isEmpty(input))
      fail(input + " is either null / empty")
    else if (!parseLong(input).exists(_ > 0))
      fail("NAN value - " + input)
    else if (input.length < 7 || input.length > 14)
      fail("Invalid UPC/EAN -" + input)
    else if (input.length == input.count(_ == '0'))
      fail("All Zero UPC not allowed. Invalid UPC/EAN - " + input)
    input
  }

  private def fail(message: String) = throw new IllegalArgumentException(message)

  private def parseLong(s: String) = Try(Some(s.toLong)).getOrElse(None)

}
