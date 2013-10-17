/**
 * @author Sebastian Riedel
 */
import org.mashupbots.socko.routes._
import org.mashupbots.socko.infrastructure.Logger
import org.mashupbots.socko.webserver.{WebServer, WebServerConfig}

import akka.actor.ActorSystem
import akka.actor.Props

/**
 * This server takes
 * - GET requests: /find/id=http://dbpedia.org/resource/David_Cameron&id=http://dbpedia.org/resource/Barack_Obama
 */
object TopicFinderServer extends Logger {
  //
  // STEP #1 - Define Actors and Start Akka
  // See `HelloHandler`
  //
  val actorSystem = ActorSystem("HelloExampleActorSystem")

  //
  // STEP #2 - Define Routes
  // Dispatch all HTTP GET events to a newly instanced `HelloHandler` actor for processing.
  // `HelloHandler` will `stop()` itself after processing each request.
  //
  val routes = Routes({
    case GET(request@Path("/find")) => {
      actorSystem.actorOf(Props[TopicFinderGetHandler]) ! request
    }
    case POST(request@Path("/find")) => {
      actorSystem.actorOf(Props[TopicFinderPostHandler]) ! request
    }
    case request =>
      println(request)
      println("Can't handle this")
  })

  //
  // STEP #3 - Start and Stop Socko Web Server
  //
  def main(args: Array[String]) {
    val webServer = new WebServer(WebServerConfig(), routes, actorSystem)
    webServer.start()

    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run { webServer.stop() }
    })

    System.out.println("Open your browser and navigate to http://localhost:8888")
  }
}

