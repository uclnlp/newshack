import akka.actor.Actor
import java.util.Date
import org.mashupbots.socko.events.HttpRequestEvent

/**
 * User: rockt
 * Date: 10/17/13
 * Time: 4:29 PM
 */

/**
 * Hello processor writes a greeting and stops.
 */
class StoryFinderGetHandler extends Actor {
  def receive = {
    case event: HttpRequestEvent =>
      val qsMap = event.endPoint.queryStringMap
      println(qsMap)
      val ids = qsMap("id")
      val stories = StoryFinder.queryCombinations(ids)
      //event.response.write("Hello from Socko (" + new Date().toString + ")")
      event.response.write("[" + stories.mkString(",\n") + "]")
      context.stop(self)
  }
}

class StoryFinderPostHandler extends Actor {
  def receive = {
    case event: HttpRequestEvent => {
      val formDataMap = event.request.content.toFormDataMap
      val text = formDataMap.get("text").get.mkString("\n")
      //the way it works is that you first do a POST with the up-to-date text and afterwards start with querying db entities
      StoryFinder.currentInputText = text
      event.response.write("Thanks for keeping me up-to-date! (" + new Date().toString + ")")
      context.stop(self)
    }
  }
}