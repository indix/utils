package com.indix.utils.core

import org.scalatest.{FlatSpec, Matchers}

class MPNTest extends FlatSpec with Matchers {

  behavior of "MPN"

  it should "check title case" in {
    MPN.isTitleCase("Key Shell") should be(true)
    MPN.isTitleCase("Samsung Galaxy A8") should be(true)
    MPN.isTitleCase("Tempered Glass") should be(true)

    MPN.isTitleCase("1442820G1") should be(false)
    MPN.isTitleCase("Macbook") should be(false)
    MPN.isTitleCase("CE 7200") should be(false)
  }

  it should "validate identifier" in {

  }

}
