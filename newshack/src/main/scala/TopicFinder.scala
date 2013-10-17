import _root_.scalaj.http.Http
import _root_.scalaj.http.HttpOptions
import org.riedelcastro.frontlets.Frontlet

/**
 * @author Sebastian Riedel
 */
object TopicFinder {

  class JuicerResult extends Frontlet {
    val coOccurences = FrontletListSlot("co-occurrences", () => new Entity)
  }

  class Entity extends Frontlet {
    val label = StringSlot("label")
  }

  def main(args: Array[String]) {
    val dbpediaIds = args
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/concepts/co-occurrences").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(
      "concept" -> "http://dbpedia.org/resource/David_Cameron",
      "type" -> "http://dbpedia.org/ontology/Person",
      "limit" -> "5"
    )
    val juicerResult = new JuicerResult().setJSON(result.asString)
    val label = juicerResult.coOccurences().head.label()
    println(label)


  }

}
