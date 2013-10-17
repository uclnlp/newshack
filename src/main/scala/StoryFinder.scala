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


  class Article extends Frontlet {
    val date = StringSlot("date")
    val uri = StringSlot("uri")
    val title = StringSlot("title")
  }

  class Story extends Frontlet {
    val date = StringSlot("date")
    val title = StringSlot("title")
    val tagged = FrontletListSlot("tagged", () => new Article)
  }


  def storyQuery(id:String, limit:Int = 5, numStories:Int = 3, numArticles: Int = 5) = {
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/things").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(
      "tag" -> id,
      "class" -> "http://purl.org/ontology/storyline/Storyline",
      "limit" -> limit.toString
    )
    val stories = new JuicerResult().setJSON("{\"stories\": " + result.asString + "}")
    // filtering stories
    val filteredStories = stories.stories().take(numStories).map(story => {
      story.tagged := story.tagged().take(numArticles)
    })

    new JuicerResult().stories(filteredStories)
  }

  def searchStory(ids:Seq[String]):Seq[Story] = {
    val perIdResults = ids.map(id => storyQuery(id).stories())
    perIdResults.map(_.toSet).reduce(_ ++ _).toSeq
  }

  val dbpediaIds = args
  val stories = searchStory(dbpediaIds)
  println(stories.mkString("\n"))
  stories.foreach(story => {
    println("Title: " + story.title())
    story.tagged().foreach(article => {
      println("\tArticle: " + article.title())
      println("\t\tURI:" + article.uri())
    })
  })
}

/*
TODO:
• AND operator for DBpedia ids
/api/things?tagop=and&tag=http://dbpedia.org/resource/David_Cameron&tag=http://dbpedia.org/resource/Syria&limit=5
*/