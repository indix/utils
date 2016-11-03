package com.indix.utils.core

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import scala.collection.JavaConverters._

class UrlUtilsSpec extends FlatSpec {

  "UrlUtils#toHostname" should "return the hostname of any given url" in {
    UrlUtils.toHostname("http://www.google.com") should be(Some("www.google.com"))
    UrlUtils.toHostname("https://www.google.com") should be(Some("www.google.com"))
    UrlUtils.toHostname("www.google.com/abc") should be(None)
  }

  "UrlUtils#toQueryMap" should "return the query params from a url as a scala map" in {
    val resMap = UrlUtils.toQueryMap("http://google.com/?query=hello&lang=en&somekey=value&")
    resMap.size should be (3)
    resMap.head should be ("query", "hello")

    val resMap1 = UrlUtils.toQueryMap("http://google.com/???query=hello&lang=en&somekey=value&")
    resMap1.size should be (3)
    resMap1.head should be ("??query", "hello")

    val resMap2 = UrlUtils.toQueryMap("http://google.com/")
    resMap2.size should be (0)
  }

  "UrlUtils#isValid" should "return true/false given the url is valid" in {
    UrlUtils.isValid("google.coma") should be(false)
    UrlUtils.isValid("https://google.com") should be(true)
  }

  "UrlUtils#resolve" should "resolve relative urls against the base url" in {
    UrlUtils.resolve("http://google.com/", "shopping") should be("http://google.com/shopping")
  }

  "UrlUtils#decode" should "UTF-8 encoded urls to unicode strings" in {
    UrlUtils.decode("http%3A%2F%2Fwww.example.com%2Fd%C3%BCsseldorf%3Fneighbourhood%3DL%C3%B6rick") should be ("http://www.example.com/düsseldorf?neighbourhood=Lörick")
  }

  "UrlUtils#encode" should "UTF-8 decoded urls to unicode strings" in {
    UrlUtils.encode("http://www.example.com/düsseldorf?neighbourhood=Lörick") should be ("http%3A%2F%2Fwww.example.com%2Fd%C3%BCsseldorf%3Fneighbourhood%3DL%C3%B6rick")
  }

  "UrlUtils#encodeSpaces" should "UTF-8 decoded urls to unicode strings" in {
    UrlUtils.encode("word1 abcd") should be ("word1%20abcd")
  }

  "UrlUtils#stripHashes" should "UTF-8 decoded urls to unicode strings" in {
    UrlUtils.stripHashes("http://www.example.com/url#fragment") should be ("http://www.example.com/url")
    UrlUtils.stripHashes("http://www.example.com/url#fragment1#fragment2") should be ("http://www.example.com/url")
  }

  "UrlUtils#addHashFragments" should "UTF-8 decoded urls to unicode strings" in {
    UrlUtils.addHashFragments("http://www.example.com/url",
      Map[String, String](
        "attr1" -> "fragment2",
        "attr2" -> "fragment 1"
      ).asJava) should be ("http://www.example.com/url#fragment2#fragment+1")
    UrlUtils.addHashFragments("http://www.example.com/url#fragment1",
      Map[String, String](
        "attr2" -> "fragment2",
        "attr1" -> "fragment3"
      ).asJava) should be ("http://www.example.com/url#fragment1#fragment3#fragment2")
  }

  "UrlUtils#get" should "UTF-8 decoded urls to unicode strings" in {
    UrlUtils.getHashFragments("http://www.example.com/url#fragment1#fragment2") should be (List("fragment1", "fragment2"))
    UrlUtils.getHashFragments("http://www.example.com/url") should be (List.empty)
  }

}
