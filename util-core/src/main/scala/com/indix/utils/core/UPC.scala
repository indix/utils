package com.indix.utils.core

import org.apache.commons.lang3.StringUtils

import scala.util.Try

case class UPC(rawUpc: Long) {

  /**
    * Standardizes a UPC in the GTIN-14 format
    * @return - Standardized UPC
    */

  def standardize : String = {

    def standardizeRec(input: String) : String = {
      if (input.length < 12) {
        standardizeRec(leftPadZeroes(input, 12))
      }
      else if (input.length == 12) {
        val cDigit = calculateCheckDigit(input.substring(0, 11))
        if (input.last == cDigit + '0') {
          input
        } else {
          val cDigit13 = calculateCheckDigit(leftPadZeroes(input, 13))
          input + cDigit13
        }
      } else {
        input
      }
    }

    leftPadZeroes(standardizeRec(rawUpc.toString), 14)
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

}

object UPC {

  /**
    * Parsers a valid UPC string to a standardized UPC (GTIN-14)
    * If the given string is not a valid UPC, the method throws an `IllegalArgumentException`
    */

  def apply(input: String) = {
    val rawUpc = verifyValidUpc(input)
    new UPC(rawUpc.toLong)
  }


  private def verifyValidUpc(input: String) = {
    if (StringUtils.isEmpty(input))
      fail(input + " is either null / empty")
    else if (parseLong(input).isEmpty)
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
