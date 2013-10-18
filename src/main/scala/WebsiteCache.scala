import scala.collection.mutable

package

/**
 * User: rockt
 * Date: 10/18/13
 * Time: 3:40 AM
 */

object WebsiteCache {
  val cache = new mutable.HashMap[String, String]

  def getHTML(URL: String): String = {
    if (cache.contains(URL)) cache(URL)
    else {
      val html = io.Source.fromURL(URL).mkString
      cache.put(URL, html)
      html
    }
  }
}