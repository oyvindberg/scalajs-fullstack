package tutorial

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

object AkkaHttpServer extends App {

  /* Akka infrastructure */
  implicit val system: ActorSystem =
    ActorSystem()

  implicit val materializer: ActorMaterializer =
    ActorMaterializer()

  /* needed for the future flatMap/onComplete in the end */
  implicit val executionContext: ExecutionContextExecutor =
    system.dispatcher

  val apiImpl: ApiImpl =
    ApiImpl(new File(".."))

  /* serve index template and static resources */
  val indexRoute: Route =
    get {
      pathSingleSlash {
        redirect("index.html", StatusCodes.MovedPermanently)
      }
    } ~
      /* when packaged (`tutorialJVM/assembly`) we find assets in the fatjar */
      getFromResourceDirectory("META-INF/resources/webjars/tutorial/0.1.0-SNAPSHOT/") ~
      /* when run from sbt (`tutorialJVM/run`) we find assets through file system */
      getFromDirectory("jvm/target/web/classes/main/META-INF/resources/webjars/tutorial/0.1.0-SNAPSHOT") ~
      AutowireAkkaHttpRoute("api", _.route[Api](apiImpl))

  Http().bindAndHandle(indexRoute, "0.0.0.0", 8080).foreach { (sb: ServerBinding) ⇒
    println(s"Server online at ${sb.localAddress}")

    Option(System.console).foreach { console ⇒
      console.readLine("Press ENTER to stop server")

      sb.unbind() // trigger unbinding from the port
        .onComplete(_ ⇒ system.terminate()) // and shutdown when done
    }
  }
}
