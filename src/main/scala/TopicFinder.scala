import _root_.scalaj.http.Http
import _root_.scalaj.http.HttpOptions
import org.riedelcastro.frontlets.Frontlet

/**
 * @author Sebastian Riedel
 */
object TopicFinder {

  class JuicerResult extends Frontlet {
    val coOccurrences = FrontletListSlot("co-occurrences", () => new Entity)
  }

  class Entity extends Frontlet {
    val label = StringSlot("label")
    val occurrence = StringSlot("occurrence")
  }


  def main(args: Array[String]) {
    val dbpediaIds = args
    val result = searchByCooccurrence(dbpediaIds)
    println(result.mkString("\n"))
  }

  def cooccurrenceQuery(id:String,typ:String = "http://dbpedia.org/ontology/Person", limit:Int = 5) = {
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/concepts/co-occurrences").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(
      "concept" -> id,
      "type" -> typ,
      "limit" -> limit.toString
    )
    new JuicerResult().setJSON(result.asString)

  }


  def searchByCooccurrence(ids:Seq[String]):Seq[Entity] = {
    val perIdResults = ids.map(id => cooccurrenceQuery(id).coOccurrences())
    val merged = perIdResults.map(_.toSet).reduce(_ ++ _)
    val sorted = merged.toSeq.sortBy(-_.occurrence().toInt)
    sorted.take(5)
  }

}
