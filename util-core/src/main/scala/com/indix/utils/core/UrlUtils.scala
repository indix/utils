package com.indix.utils.core

import java.net.{URI, URLDecoder, URL}

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

  /** Decode a given url using UTF-8 encoding
    *
    * @param url Input url
    * @return Decoded url
    */
  def decode(url: String) = URLDecoder.decode(url, "UTF-8")


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
