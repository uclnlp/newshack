import scala.xml.Node

/**
 * Created with IntelliJ IDEA.
 * User: Giorgos
 * Date: 17/10/13
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */

object ArticleParser {

  def similarity( a:Map[String,Int],b:Map[String,Int] ): Double = {
    val asize=Math.sqrt(a.mapValues(x=>x*x).foldLeft(0)(_+_._2))
    val bsize=Math.sqrt(b.mapValues(x=>x*x).foldLeft(0)(_+_._2))
    val prod = (a.keySet ++ b.keySet).map (i=> (i,a.getOrElse(i,0)*b.getOrElse(i,0))).toMap.foldLeft(0)(_+_._2)
    prod/(asize*bsize)
  }

  def extract(URL: String): Array[String] = {
    val html = io.Source.fromURL(URL).mkString
    val parser = new HTML5Parser
    val parsed = parser.loadString(html)
    var story: Node = null
    var article = Array("", "")
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

  def getNormVector(article:String):Map[String,Int]={
    val symbols = Seq(".", ",", "(", ")", "\"","[","]", "!", "'")
    var doc =article.toLowerCase()
    //remove punctuation
    for (symbol <- symbols) {
      doc=doc.replace(symbol, "")
    }
    val seq=doc.split(" ")
    seq.groupBy(identity).mapValues(_.length)
  }

  def main(args: Array[String]) {
    val URL1="http://www.bbc.co.uk/news/uk-24530186"
    val URL2 = "http://www.bbc.co.uk/news/uk-24530186"
    val URL3 ="http://www.bbc.co.uk/news/uk-24575059"

    val article1 = extract(URL1)
    val article2 = extract(URL2)
    val article3 = extract(URL3)
    //println("TITLE: " + article1(0))
    //println("BODY: " + article1(1))
    val mapA=getNormVector(article1.mkString(" "))
    val mapB=getNormVector(article2.mkString(" "))
    val mapC=getNormVector(article3.mkString(" "))
    println(similarity(mapA,mapB))
    println(similarity(mapA,mapC))
  }

}
