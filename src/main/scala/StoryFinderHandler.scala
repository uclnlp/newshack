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
      val stories = StoryFinder.storyQueryWithMultipleIds(ids)
      //event.response.write("Hello from Socko (" + new Date().toString + ")")
      event.response.write("[" + stories.mkString(",\n") + "]")
      context.stop(self)
  }
}

class StoryFinderPostHandler extends Actor {
  def receive = {
    case event: HttpRequestEvent =>
      val formDataMap = event.request.content.toFormDataMap
      println(formDataMap)
      println(event.request.content.toString())
      println(event.request.content.toFormDataMap())
      event.response.write("Hello from Socko POST (" + new Date().toString + ")")
      context.stop(self)
  }
}