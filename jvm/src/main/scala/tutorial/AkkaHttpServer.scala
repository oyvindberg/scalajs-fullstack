package tutorial

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ScalatagsHandler._

import scala.concurrent.{ExecutionContextExecutor, Future}

object AkkaHttpServer extends App {

  /* Akka infrastructure */
  implicit val system: ActorSystem =
    ActorSystem()

  implicit val materializer: ActorMaterializer =
    ActorMaterializer()

  /* needed for the future flatMap/onComplete in the end */
  implicit val executionContext: ExecutionContextExecutor =
    system.dispatcher

  val corsHeaders: List[ModeledHeader] =
    List(
      `Access-Control-Allow-Methods`(HttpMethods.OPTIONS, HttpMethods.GET, HttpMethods.POST),
      `Access-Control-Allow-Origin`(HttpOriginRange.*),
      `Access-Control-Allow-Headers`(
        "Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent"),
      `Access-Control-Max-Age`(1728000)
    )

  /* serve index template and static resources */
  val indexRoute: Route =
    pathPrefix("js"){
      getFromResourceDirectory("public")
    } ~
    pathPrefix("img") {
      getFromResourceDirectory("public/img")
    } ~
    get {
      pathSingleSlash {
        complete {
          Template.asScalaTags
        }
      } ~ getFromResourceDirectory("")
    } ~ options {
      complete(HttpResponse(headers = corsHeaders))
    }

  val impl: ApiImpl =
    ApiImpl(new File(".."))

  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(indexRoute ~ AutowireAkkaHttpRoute("api", _.route[Api](impl)),
                         "0.0.0.0",
                         8080)

  bindingFuture.foreach { (sb: ServerBinding) ⇒
    println(s"Server online at ${sb.localAddress}")

    Option(System.console).foreach { console ⇒
      console.readLine("Press ENTER to stop server")

      sb.unbind() // trigger unbinding from the port
        .onComplete(_ ⇒ system.terminate()) // and shutdown when done
    }
  }
}
