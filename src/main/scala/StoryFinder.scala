import org.riedelcastro.frontlets.Frontlet
import scalaj.http.{HttpOptions, Http}

/**
 * User: rockt
 * Date: 10/17/13
 * Time: 3:46 PM
 */

object StoryFinder extends App {
  class JuicerResult extends Frontlet {
    val stories = FrontletListSlot("stories", () => new Story)
  }

  class Story extends Frontlet {
    val label = StringSlot("label")
    //val occurrence = StringSlot("occurrence")
  }

  def storyQuery(id:String, limit:Int = 5) = {
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/things").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(
      "tag" -> id,
      "class" -> "http://purl.org/ontology/storyline/Storyline",
      "limit" -> limit.toString
    )
    new JuicerResult().setJSON("{\"stories\": " + result.asString + "}")
  }

  def searchStory(ids:Seq[String]):Seq[Story] = {
    val perIdResults = ids.map(id => storyQuery(id).stories())
    val merged = perIdResults.map(_.toSet).reduce(_ ++ _)
    merged.take(5).toSeq
  }
  
  val dbpediaIds = args
  val result = searchStory(dbpediaIds)
  println(result.mkString("\n"))
}

/*
TODO:
• AND operator for DBpedia ids
/api/things?tagop=and&tag=http://dbpedia.org/resource/David_Cameron&tag=http://dbpedia.org/resource/Syria&limit=5
*/