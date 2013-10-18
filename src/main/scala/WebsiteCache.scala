import scala.collection.mutable
import scala.xml.Node

package

/**
 * User: rockt
 * Date: 10/18/13
 * Time: 3:40 AM
 */

object WebsiteCache {
  val cache = new mutable.HashMap[String, (String, String)]

  def getHTML(URL: String): (String, String) = {
    if (cache.contains(URL)) cache(URL)
    else {
      var (header, body) = ("", "")
      val html = io.Source.fromURL(URL).mkString
      val start = System.currentTimeMillis()
      val parser = new HTML5Parser
      val parsed = parser.loadString(html)
      var story: Node = null
      try {
        for (n <- (parsed \\ "div")) {
          if ((n \ "@class").text == "story-body")
            story = n
        }
        for (n <- (story \\ "h1"))
          if ((n \ "@class").text == "story-header")
            header = n.text
        body = (story \\ "p").map(_.text).filter(_ != "Please turn on JavaScript. Media requires JavaScript to play.").mkString(" ")
      } catch {
        case ne: NullPointerException => /* ignore */
      }

      cache.put(URL, (header,body))
      print("parsing [" + (System.currentTimeMillis() - start) + "ms] ")
      (header, body)
    }
  }
}