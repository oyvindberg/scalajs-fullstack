package tutorial

import org.scalajs.dom.ext.Ajax
import upickle.Js
import upickle.default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

// client-side implementation, and call-site
object AutowireClient extends autowire.Client[Js.Value, Reader, Writer] {
  def write[Result: Writer](r: Result): Js.Value =
    writeJs(r)

  def read[Result: Reader](p: Js.Value): Result =
    readJs[Result](p)

  override def doCall(req: Request): Future[Js.Value] =
    Ajax
      .post(
        url = s"api/${req.path.mkString("/")}",
        data = upickle.json.write(Js.Obj(req.args.toSeq: _*))
      )
      .map(req ⇒ upickle.json.read(req.responseText))
}
