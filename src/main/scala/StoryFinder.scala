import org.riedelcastro.frontlets.Frontlet
import scalaj.http.{HttpOptions, Http}

/**
 * User: rockt
 * Date: 10/17/13
 * Time: 3:46 PM
 */

object StoryFinder extends App {
  var currentInputText: String = ""

  class JuicerResult extends Frontlet {
    val stories = FrontletListSlot("stories", () => new Story)
    val refId = StringListSlot("refId")
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

  def queryCombinations(ids: Seq[String], numCombinations: Int = 5) = {
    val queryCombinations =
      if (ids.size < numCombinations && ids.size >= 2) {
        for {
          i <- (2 to numCombinations).reverse
          tags <- ids.combinations(i)
        } yield queryWithMultipleIds(tags)
      } else Nil
    val combinedQuery = queryWithMultipleIds(ids)
    val singleQueries = ids.map(id => queryWithMultipleIds(List(id)))

    val result = (queryCombinations ++ singleQueries).filter(story => !story.stories().isEmpty)

    if (ids.size > numCombinations && !combinedQuery.stories().isEmpty) combinedQuery :: result.toList
    else result.toList
  }

  def dummySimFunction(string1: String, string2: String): Double = if (string1.isEmpty || string2.isEmpty) 0.0 else 1.0

  def storyToString(story: Story): String =
    story.tagged().map(article => {
      //rockt: too slow, hence currently commented out
      //ArticleParser.extract(article.uri())
      ""
    }).mkString("\n")
  
  def queryWithMultipleIds(ids:Seq[String], limit:Int = 5, numStories:Int = 3, numArticles: Int = 5, numIdCombinations: Int = 5) = {
    val parameters = ids.map(id => ("tag",id)).toList ++
      List("class" -> "http://purl.org/ontology/storyline/Storyline",
           //fetching a bit more stories to have enough instances for ranking
           "limit" -> (limit*2).toString,
           "tagop" -> "and")
    val result = Http("http://triplestore.bbcnewslabs.co.uk//api/things").
      option(_.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")).
      option(HttpOptions.connTimeout(10000)).
      option(HttpOptions.readTimeout(10000)).params(parameters)
    
    val stories = new JuicerResult().setJSON("{\"stories\": " + result.asString + "}").stories()

    //rank stories by similarity to input text
    val sortedStories = stories.map(story => (story, dummySimFunction(currentInputText, storyToString(story)))).sortBy(_._2)

    //using only top related stories
    val filteredStories = sortedStories.take(numStories).map(_._1).map(story => {
      //using only most similar articles
      val sortedArticles = story.tagged()
        .map(article => (article, dummySimFunction(currentInputText, ArticleParser.extract(article.uri()).mkString("\n"))))
      story.tagged := sortedArticles.take(numArticles).map(_._1)
    })
    val resultStories = new JuicerResult().stories(filteredStories)
    resultStories.refId := ids
    resultStories
  }

  val dbpediaIds = args
  //val stories = searchStory(dbpediaIds)
  val stories = queryCombinations(dbpediaIds)
  stories.foreach(s => {
    println("Ids: " + s.refId().mkString(", "))
    s.stories().foreach(story => {
    println("\tTitle: " + story.title())
    story.tagged().foreach(article => {
      println("\t\tArticle: " + article.title())
      println("\t\t\tURI:" + article.uri())
    })
  })}
  )
}

/*
Examples:

will give you the storyline with David and Nigel before story lines that contain only one of them:
/find/story?id=http://dbpedia.org/resource/David_Cameron&id=http://dbpedia.org/resource/Nigel_Farage&limit=5


*/