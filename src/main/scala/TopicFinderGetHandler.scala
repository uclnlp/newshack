import org.mashupbots.socko.events.HttpRequestEvent
import akka.actor.Actor
import java.util.Date

/**
 * Hello processor writes a greeting and stops.
 */
class TopicFinderGetHandler extends Actor {
  def receive = {
    case event: HttpRequestEvent =>
      val qsMap = event.endPoint.queryStringMap
      println(qsMap)
      val ids = qsMap("id")
      val result = TopicFinder.searchByCooccurrence(ids)
      //event.response.write("Hello from Socko (" + new Date().toString + ")")
      event.response.write("[" + result.mkString(",\n") + "]")
      context.stop(self)
  }
}

class TopicFinderPostHandler extends Actor {
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