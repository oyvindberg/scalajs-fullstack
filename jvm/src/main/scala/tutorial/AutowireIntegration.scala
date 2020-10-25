package tutorial

import upickle.default.{Reader, Writer, read => readJson, write => writeJson}

/** Since we are using micro-libraries, it often falls to the
  * user to do the integration between them.
  *
  * This needs to be done for every combination of
  * autowire + a serialization library + a HTTP library.
  *
  * You're in luck, though, because here it is provided for you
  */
/* integration between Autowire and uPickle */
object AutowireUpickleServer extends autowire.Server[String, Reader, Writer] {
  def read[Result: Reader](p: String): Result =
    readJson[Result](p)

  def write[Result: Writer](r: Result): String =
    writeJson(r)
}

/* integration between Autowire/uPickle and Akka Http */
object AutowireAkkaHttpRoute {

  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.server._

  /** @param f Need to expose this to user in order to not break macro
    * @return Akka Http route
    */
  def apply(uri: PathMatcher[Unit], f: AutowireUpickleServer.type => AutowireUpickleServer.Router): Route =
    post {
      path(uri / Segments) { paths: List[String] =>
        entity(as[String]) { argsString =>
          complete {
            val decodedArgs: Map[String, String] =
              readJson[List[(String, String)]](argsString).toMap

            val router: AutowireUpickleServer.Router =
              f(AutowireUpickleServer)

            router(autowire.Core.Request(paths, decodedArgs))
          }
        }
      }
    }
}
