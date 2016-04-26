package tutorial

import upickle.Js
import upickle.Js.Value
import upickle.default.{Reader, Writer}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Since we are using micro-libraries, it often falls to the
  *  user to do the integration between them.
  *
  * This needs to be done for every combination of
  * autowire + a serialization library + a HTTP library.
  *
  * You're in luck, though, because here it is provided for you
  */

/* integration between Autowire and uPickle */
object AutowireUpickleServer extends autowire.Server[Js.Value, Reader, Writer]{
  def read[Result: Reader](p: Js.Value): Result =
    upickle.default.readJs[Result](p)

  def write[Result: Writer](r: Result): Value =
    upickle.default.writeJs(r)
}

/* integration between Autowire/uPickle and Akka Http */
object AutowireAkkaHttpRoute {
  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.server._

  /**
    * @param f Need to expose this to user in order to not break macro
    * @return Akka Http route
    */
  def apply(uri:          PathMatcher[Unit],
            f:            AutowireUpickleServer.type ⇒ AutowireUpickleServer.Router)
           (implicit ctx: ExecutionContext): Route = {

    post {
      path(uri / Segments) {
        (path: List[String]) =>
          entity(as[String]) {
            (argsString: String) ⇒
              complete {
                val decodedArgs: Map[String, Js.Value] =
                  upickle.json.read(argsString).asInstanceOf[Js.Obj].value.toMap

                val router: AutowireUpickleServer.Router =
                  f(AutowireUpickleServer)

                val result: Future[Js.Value] =
                  router(autowire.Core.Request(path, decodedArgs))

                val serializedResult: Future[String] =
                  result.map(upickle.json.write(_))

                serializedResult
              }
          }
      }
    }
  }
}
