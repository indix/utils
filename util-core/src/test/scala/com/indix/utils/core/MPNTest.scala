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
    MPN.isValidIdentifier("") should be (false)
    MPN.isValidIdentifier("51") should be (false)
    MPN.isValidIdentifier("NA") should be (false)
    MPN.isValidIdentifier("Does not apply") should be (false)


    MPN.isValidIdentifier("DT.VFGAA.003") should be (true)
    MPN.isValidIdentifier("A55BM-A/USB3") should be (true)
    MPN.isValidIdentifier("cASSP1598345-10") should be (true)
    MPN.isValidIdentifier("016393B119058-Regular-18x30-BE-BK") should be (true)
    MPN.isValidIdentifier("PJS2V") should be (true)
  }

}
