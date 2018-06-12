package com.indix.utils.core

import org.scalatest.{FlatSpec, Matchers}

class UPCSpec extends FlatSpec with Matchers {

  "UPC" should "convert a UPC to a standardized format" in {
    UPC.standardize("63938200039") should be("00639382000393")
    UPC.standardize("99999999623") should be("00999999996237")
    UPC.standardize("89504500098") should be("00895045000982")
    UPC.standardize("934093502349") should be("09340935023493")
    UPC.standardize("841106172217") should be("08411061722176")
    UPC.standardize("810000439") should be("00008100004393")
    UPC.standardize("931177059140") should be("09311770591409")
    UPC.standardize("9311770591409") should be("09311770591409")
    UPC.standardize("27242860940") should be("00027242860940")
    UPC.standardize("75317405253") should be("00753174052534")
    UPC.standardize("-810000439") should be("00008100004393")
    UPC.standardize("810-000-439") should be("00008100004393")

    // Iphone UPCs
    UPC.standardize("885909950652") should be("00885909950652")
    UPC.standardize("715660702866") should be("00715660702866")
  }

  it should "work correctly for GTIN UPCs by converting it to a valid EAN-13 with padded zeros" in {
    UPC.standardize("10010942220401") should be ("00010942220404")
    UPC.standardize("47111850104013") should be("07111850104015")
    UPC.standardize("40628043604719") should be("00628043604711")
  }


  it should "work correctly for ISBN numbers" in {
    UPC.standardize("978052549832") should be("9780525498322")
    UPC.standardize("9780500517260") should be("9780500517260")
    UPC.standardize("9780316512787") should be("9780316512787")
    UPC.standardize("9780997355932") should be("9780997355932")
  }

  it should "not replace check-digit if UPC already left padded and check-digit and last-digit same" in {
    UPC.standardize("0753174052534") should be("00753174052534")
  }

  it should "fail for an invalid 14-digit UPC (GTIN)" in {
    intercept[IllegalArgumentException] {
      UPC.standardize("47111850104010")
    }
  }

  it should "fail for all zeroes UPC" in {
    intercept[IllegalArgumentException] {
      UPC.standardize("00000000000")
    }
  }

  it should "fail for invalid UPC" in {
    intercept[IllegalArgumentException] {
      UPC.standardize("12345")
    }
  }

  it should "fail for empty or null UPC" in {
    intercept[IllegalArgumentException] {
      UPC.standardize("")
    }
  }

  it should "fail in case UPC is not a number" in {
    intercept[IllegalArgumentException] {
      UPC.standardize("ABCD")
    }
  }
}