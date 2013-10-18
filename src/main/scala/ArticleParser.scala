import scala.xml.Node
import WebsiteCache._

/**
 * Created with IntelliJ IDEA.
 * User: Giorgos
 * Date: 17/10/13
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */

object ArticleParser {
  lazy val stopwords = io.Source.fromURL("http://www.textfixer.com/resources/common-english-words.txt").mkString.split(",")

  def similarity(a: Map[String, Int], b: Map[String, Int]): Double = {
    if (a.isEmpty || b.isEmpty)
      0
    else {
      val asize = Math.sqrt(a.mapValues(x => x * x).foldLeft(0)(_ + _._2))
      val bsize = Math.sqrt(b.mapValues(x => x * x).foldLeft(0)(_ + _._2))
      val prod = (a.keySet ++ b.keySet).map(i => (i, a.getOrElse(i, 0) * b.getOrElse(i, 0))).toMap.foldLeft(0)(_ + _._2)
      prod / (asize * bsize)
    }
  }

  def similarity(string1: String, string2: String): Double =
    similarity(getNormVector(string1,stopwords), getNormVector(string2,stopwords))

  def time(f: => Unit) = {
    val s = System.currentTimeMillis
    f
    System.currentTimeMillis - s
  }

  def extract(URL: String): String = {
    if (URL.startsWith("http://www.bbc.co.uk/news")) {
      val start = System.currentTimeMillis()
      print("processing " + URL + "... ")
      val tuple = getHTML(URL)
      println((System.currentTimeMillis() - start)/1000.0 + "ms")
      tuple._1 + "\n" + tuple._2
    } else ""
  }

  def getNormVector(article: String, stopwords: Array[String]): Map[String, Int] = {
    val symbols = Seq(".", ",", "(", ")", "\"", "[", "]", "!", "'")
    var doc = article.toLowerCase()
    //remove punctuation
    for (symbol <- symbols) {
      doc = doc.replace(symbol, "")
    }
    var seq = doc.split(" ")
    for (word <- stopwords) {
      seq = seq.filter(_ != word)
    }
    seq.groupBy(identity).mapValues(_.length)
  }

  def main(args: Array[String]) {
    val URL1 = "http://www.bbc.co.uk/news/uk-24530186"
    val URL2 = "http://www.bbc.co.uk/news/uk-politics-24553611"
    val URL3 = "http://www.bbc.co.uk/news/uk-24575059"
    val article1 = extract(URL1)
    val article2 = extract(URL2)
    val article3 = extract(URL3)
    //println("TITLE: " + article1(0))
    //println("BODY: " + article1(1))
    val mapA = getNormVector(article1.mkString(" "), stopwords)
    val mapB = getNormVector(article2.mkString(" "), stopwords)
    val mapC = getNormVector(article3.mkString(" "), stopwords)
    println(similarity(mapA, mapB))
    println(similarity(mapA, mapC))
  }

}
