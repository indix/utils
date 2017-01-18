package com.indix.utils.core

import org.scalatest.{FlatSpec, Matchers}

class UPCSpec extends FlatSpec with Matchers {

  it should "convert a UPC to a standardized format" in {
    UPC("63938200039").standardize should be("00639382000393")
    UPC("97850001830").standardize should be("00978500018309")
    UPC("934093502349").standardize should be("09340935023493")
    UPC("99999999623").standardize should be("00999999996237")
    UPC("89504500098").standardize should be("00895045000982")
    UPC("841106172217").standardize should be("08411061722176")
    UPC("810000439").standardize should be("00008100004393")
    UPC("931177059140").standardize should be("09311770591409")
    UPC("9311770591409").standardize should be("09311770591409")
    UPC("27242860940").standardize should be("00027242860940")
    UPC("75317405253").standardize should be("00753174052534")
    UPC("0753174052534").standardize should be("00753174052534")
    UPC("-810000439").standardize should be("00008100004393")
    UPC("810-000-439").standardize should be("00008100004393")
  }

  it should "fail for all zeroes UPC" in {
    intercept[IllegalArgumentException] {
      UPC("00000000000").standardize
    }
  }

  it should "fail for invalid UPC" in {
    intercept[IllegalArgumentException] {
      UPC("12345").standardize
    }
  }

  it should "fail for empty or null UPC" in {
    intercept[IllegalArgumentException] {
      UPC("").standardize
    }
  }

  it should "fail in case UPC is not a number" in {
    intercept[IllegalArgumentException] {
      UPC("ABCD").standardize
    }
  }
}