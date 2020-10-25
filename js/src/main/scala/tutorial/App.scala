package tutorial

import autowire._
import org.scalajs.dom
import slinky.web.ReactDOM
import typings.react.components._
import typings.react.mod.CSSProperties

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object App {
  implicit val ec = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  @JSImport("./assets/img/logo.svg", JSImport.Default)
  @js.native
  val Logo: String = js.native

  @JSImport("antd/dist/antd.css", JSImport.Namespace)
  @js.native
  object Css extends js.Object

  def main(args: Array[String]): Unit = {
    Css // touch to load

    ReactDOM.render(
      div(
        header(img.src(Logo)).style(CSSProperties().setWidth("300px").setPadding("25px")),
        FileBrowser(remoteFetchPaths = path => AutowireClient[Api].fetchPathsUnder(path).call())
      ),
      dom.document.body
    )
  }
}
