package tutorial

import org.scalajs.dom.ext.Ajax
import upickle.default
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// client-side implementation, and call-site
object AutowireClient extends autowire.Client[String, Reader, Writer] {
  def write[Result: Writer](r: Result): String = default.write(r)

  def read[Result: Reader](p: String): Result = default.read[Result](p)

  override def doCall(req: Request): Future[String] =
    Ajax
      .post(
        url  = s"api/${req.path.mkString("/")}",
        data = default.write(req.args.toSeq)
      )
      .map(req => req.responseText)
}
