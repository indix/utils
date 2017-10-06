package com.indix.utils.core

import org.scalatest.{FlatSpec, Matchers}

class ISBNSpec extends FlatSpec with Matchers {
  "ISBN" should "create a valid ISBN object for" in {
    ISBN("0-306-40615-2").get.isbn10 should be (Some("0306406152"))
    ISBN("0-306-40615-2").get.isbn should be ("9780306406157")
    ISBN("978-0-306-40615-7").get.isbn should be ("9780306406157")
    ISBN("978-0-306-40615-7").get.isbn10 should be (None)
    ISBN("9971502100").get.isbn10 should be (Some("9971502100"))
    ISBN("9971502100").get.isbn should be ("9789971502102")
    ISBN("960 425 059 0").get.isbn10 should be (Some("9604250590"))
    ISBN("960 425 059 0").get.isbn should be ("9789604250592")
  }

  it should "not create a valid ISBN object for" in {
    ISBN("abcd") should be (None)
    ISBN("123") should be (None)
    ISBN("") should be (None)
    ISBN("  ") should be (None)
    ISBN("-") should be (None)
    ISBN("1234567890") should be (None)
    ISBN("1234567890111") should be (None)
    ISBN("0-306-40615-1") should be (None)
    ISBN("978-0-306-40615-5") should be (None)
  }
}
