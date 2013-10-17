import scala.xml._
import scala.xml.parsing.NoBindingFactoryAdapter

/**
 * @author Sebastian Riedel
 */
/**
 * @author sriedel
 */
class HTML5Parser extends NoBindingFactoryAdapter {

  override def loadXML(source : InputSource, _p: SAXParser) = {
    loadXML(source)
  }

  def loadXML(source : InputSource) = {
    import nu.validator.htmlparser.{sax,common}
    import sax.HtmlParser
    import common.XmlViolationPolicy

    val reader = new HtmlParser
    reader.setXmlPolicy(XmlViolationPolicy.ALLOW)
    reader.setContentHandler(this)
    reader.parse(source)
    rootElem
  }
}

object XMLImplicits {
  implicit def node2richNode(node: NodeSeq) = new AnyRef {
    def has(attr: String, value: String) = node.filter(_.attribute(attr).exists(_.text == value))
  }

}


object HTML5ParserExample {
  import XMLImplicits._

  def main(args: Array[String]) {
    val someHTML = "<html><body><div>Blah</div>blah</body></html>"
    val parser = new HTML5Parser
    val parsed = parser.loadString(someHTML)
    println(parsed \\ "div")

  }
}