package com.indix.utils.core

import java.net.{URI, URLDecoder,URLEncoder, URL}
import java.util.{Map => JMap, TreeMap => JTreeMap}
import org.apache.commons.lang3.text.WordUtils

import scala.collection.JavaConversions._

import scala.util.Try

/** Useful helper methods to operate on product urls */
object UrlUtils {

  /** extract hostname from a given url
    *
    * @param url Input url
    * @return Extracted hostname
    *
    */
  def toHostname(url: String): Option[String] = Try(new URL(url)).map(_.getHost.toLowerCase).toOption

  /** Find if the url is valid one or not
    *
    * @param url Input url
    * @return if the url is valid or not
    */
  def isValid(url: String): Boolean = {
    try { new URL(url).getHost; true } catch { case _: Throwable => false }
  }

  /**
    *
    * @param pageUrl The base url. ie the url of the page
    * @param extractedUrl Url extracted from the page. Mostly a relative url segment
    * @return The extractedUrl segment resolved against the base url of the page
    */
  def resolve(pageUrl: String, extractedUrl: String): String = {
    URI.create(pageUrl).normalize().resolve(extractedUrl).normalize().toString
  }

  /** Decode a given string using UTF-8 encoding
    *
    * @param segment Input url segment
    * @return Decoded segment
    */
  def decode(segment: String) = URLDecoder.decode(segment, "UTF-8")
  
  /** Encode a given string using UTF-8 encoding
    *
    * @param segment Input url segment
    * @return Encoded segment
    */
  def encode(segment: String) = URLEncoder.encode(segment, "UTF-8").replace("+", "%20")

  /**
    *
    * @param url Input url
    * @return URL query params(key-value pairs) as scala Map
    */
  def toQueryMap(url: String): Map[String, String] = {
    val query = try {
      Option(new URL(url).getQuery).getOrElse("")
    } catch {
      case e: Throwable => ""
    }
    queryStringMap(query)
  }

  /**
    *
    * @param url Input url
    * @return Url without hash fragments
    */
  def stripHashes(url: String) = url.replaceAll("#.*$", "")

  /**
    *
    * @param url Input url
    * @param attributes Map of attributes whose values will be appended to the url as hash fragments
    * @return Url with hash fragments appended in sorted order of keys, with values in title case
    */
  def addHashFragments(url: String, attributes: JMap[String, String]) = {
    new JTreeMap[String, String](attributes)
      .foldLeft(url) {
        case (resultingURL, attribute) => resultingURL + "#" + convertToUrlFragment(attribute._2)
      }
  }

  /**
    *
    * @param fragment Input fragment
    * @return Fragment that is url encoded and in title case
    */
  def convertToUrlFragment(fragment: String) = {
    URLEncoder.encode(WordUtils.capitalize(fragment, ' ', '-', '/'), "UTF-8")
  }

  /**
    *
    * @param url Input url
    * @return List of hash fragments from the url
    */
  def getHashFragments(url: String) = url.split("#").drop(1)

  private def queryStringMap(query: String) = {
    val parts = query.split("&")
    parts.filter(_.nonEmpty).map {
      p =>
        val q = p.split("=")
        q.length match {
          case 2 => URLDecoder.decode(q(0), "UTF-8") -> URLDecoder.decode(q(1), "UTF-8")
          case 1 => URLDecoder.decode(q(0), "UTF-8") -> ""
        }
    }.toMap
  }
}
