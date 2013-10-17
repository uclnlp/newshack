import scala.xml.Node

/**
 * Created with IntelliJ IDEA.
 * User: Giorgos
 * Date: 17/10/13
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */

object ArticleParser {

  def extract(URL: String): Array[String] = {
    val html = io.Source.fromURL(URL).mkString
    val parser = new HTML5Parser
    val parsed = parser.loadString(html)
    var story: Node = null
    var article= Array("", "")
    for (n <- (parsed \\ "div")) {
      if ((n \ "@class").text == "story-body")
        story = n
    }
    for (n <- (story \\ "h1"))
      if ((n \ "@class").text == "story-header")
        article(0) = n.text
    article(1) = (story \\ "p").map(_.text).filter(_ != "Please turn on JavaScript. Media requires JavaScript to play.").mkString(" ")
    article
  }

  def main(args: Array[String]) {
    //val URL="http://www.bbc.co.uk/news/uk-politics-24553611"
    val URL = "http://www.bbc.co.uk/news/uk-england-derbyshire-24548690"
    val article=extract(URL)
    println("TITLE: " + article(0))
    println("BODY: " + article(1))
  }

}
