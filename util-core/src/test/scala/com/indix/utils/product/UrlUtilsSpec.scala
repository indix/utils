package com.indix.utils.product


import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.com.indix.utils.product.UrlUtils

class UrlUtilsSpec extends FlatSpec {

  "UrlUtils#toHostname" should "return the hostname of any given url" in {
    UrlUtils.toHostname("http://www.google.com") should be("www.google.com")
    UrlUtils.toHostname("https://www.google.com") should be("www.google.com")
    UrlUtils.toHostname("www.google.com/abc") should be("www.google.com")
  }

  "UrlUtils#toQueryMap" should "return the query params from a url as a scala map" in {
    val resMap = UrlUtils.toQueryMap("http://google.com/?query=hello&lang=en&somekey=value&")
    resMap.size should be (3)
    resMap.head should be ("query", "hello")
  }

}
