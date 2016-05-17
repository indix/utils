package scala.com.indix.utils.product

import java.net.{URLDecoder, URL}

/** Useful helper methods to operate on product urls */
object UrlUtils {

  /** extract hostname from a given url
    *
    * @param url Input url
    * @return Extracted hostname
    *
    */
  def toHostname(url: String): String = {
    val modifiedUrl = url.contains("http:") || url.contains("https:") match {
      case true => url
      case false => s"http://$url"
    }

    val s = modifiedUrl.split("/")
    s.length match {
      case x if x >=3 => s(2).split('?')(0)
      case _ => url
    }
  }

  /**
    *
    * @param url Input url
    * @return URL query params(key-value pairs) as [[Map]]
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
    parts.map {
      p =>
        val q = p.split("=")
        q.length match {
          case 2 => URLDecoder.decode(q(0), "UTF-8") -> URLDecoder.decode(q(1), "UTF-8")
          case 1 => URLDecoder.decode(q(0), "UTF-8") -> ""
        }
    }.toMap
  }
}
