import org.riedelcastro.frontlets.Frontlet
import scala.collection.mutable
import scalaj.http.{HttpOptions, Http}
import ArticleParser._

/**
 * User: rockt
 * Date: 10/17/13
 * Time: 3:46 PM
 */

object StoryFinder extends App {
  var currentInputText: String = ""

  class StoriesContainer extends Frontlet {
    val stories = FrontletListSlot("stories", () => new Story)
    val refId = StringListSlot("refId")
    val sim = DoubleSlot("sim")
  }

  class Article extends Frontlet {
    val date = StringSlot("date")
    val uri = StringSlot("uri")
    val title = StringSlot("title")
    val sim = DoubleSlot("sim")
  }

  class Story extends Frontlet {
    val date = StringSlot("date")
    val title = StringSlot("title")
    val tagged = FrontletListSlot("tagged", () => new Article)
    val sim = DoubleSlot("sim")
  }  

  def queryCombinations(ids: Seq[String], numCombinations: Int = 5, numStoryContainers: Int = 4) = {
    val distinctIds = ids.toList.distinct
    val queryCombinations =
      if (distinctIds.size <= 2*numCombinations && distinctIds.size >= 2) {
        for {
          i <- (2 to numCombinations).reverse
          tags <- distinctIds.combinations(i)
        } yield queryWithMultipleIds(tags)
      } else Nil
    val combinedQuery = queryWithMultipleIds(distinctIds)
    val singleQueries = distinctIds.map(id => queryWithMultipleIds(List(id)))

    val tempResult = (queryCombinations ++ singleQueries).filter(story => !story.stories().isEmpty)

    if (distinctIds.size > numCombinations && !combinedQuery.stories().isEmpty) combinedQuery :: tempResult.toList
    else tempResult.toList

    val sortedResult = tempResult.sortBy(-_.sim()).take(numStoryContainers)

    val coveredStories = new mutable.HashSet[String]()

    //getting rid of stories already covered in another combination of entities
    sortedResult.foreach(container => {
      val filteredStories = container.stories().filter(story => {
        val title = story.title()
        if (coveredStories.contains(title)) false
        else {
          coveredStories.add(title)
          true
        }
      })
      container.stories := filteredStories
    })




    //re-calculating container similarities
    sortedResult.foreach(container => {
      val stories = container.stories()
      val sims = List(1.0)
      var aggr = 0.0
      var counter = 0
      stories.map(s => {
        aggr += s.sim()
        counter += 1
        s
      }) //cumbersome, but otherwise the stories get erased. bug in Frontlet?
      container.sim := (if (counter == 0) -1.0 else aggr / counter)
      container
    })

    sortedResult.sortBy(-_.sim())
  }

  def storyToString(story: Story): String =
    story.tagged().map(article => extract(article.uri())).mkString("\n")
  
  def queryWithMultipleIds(ids:Seq[String], limit:Int = 5, numStories:Int = 3, numArticles: Int = 5, numIdCombinations: Int = 5) = {
    val parameters = ids.distinct.map(id => ("tag",id)).toList ++
      List("class" -> "http://purl.org/ontology/storyline/Storyline",
           "limit" -> limit.toString,
           "tagop" -> "and")
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/things").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(parameters)
    
    val stories = new StoriesContainer().setJSON("{\"stories\": " + result.asString + "}").stories()

    //using only top related stories
    val filteredStories = stories.take(numStories)
      .map(story => {
        //using only most similar articles
        val sortedArticles = story.tagged().take(numArticles*2)
          .map(article => (article, similarity(currentInputText, extract(article.uri())))).sortBy(-_._2)
        story.tagged := sortedArticles.take(numArticles).map(t => {
          val article = t._1
          article.sim := t._2
          article
        })
      }).map(story => {
        val sims = story.tagged().map(_.sim())
        story.sim := sims.sum / (1.0 * sims.size)
        story
      }).sortBy(-_.sim()).take(numStories)
    val resultStories = new StoriesContainer().stories(filteredStories)
    resultStories.refId := ids
    val sims = filteredStories.map(_.sim())
    resultStories.sim := sims.sum / (1.0 * sims.size)
    resultStories
  }

  val dbpediaIds = args
  //val stories = searchStory(dbpediaIds)
  val stories = queryCombinations(dbpediaIds)
  stories.foreach(s => {
    println("Ids: " + s.refId().mkString(", "))
    s.stories().foreach(story => {
    println("\tTitle: " + story.title())
/*    story.tagged().foreach(article => {
      println("\t\tArticle: " + article.title())
      println("\t\t\tURI:" + article.uri())
    })
*/
  })}
  )
}

/*
Examples:

will give you the storyline with David and Nigel before story lines that contain only one of them:
/find/story?id=http://dbpedia.org/resource/David_Cameron&id=http://dbpedia.org/resource/Nigel_Farage&limit=5


*/