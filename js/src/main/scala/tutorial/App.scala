package tutorial

import autowire._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, HTMLStyleElement}
import scalacss.ScalatagsCss._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import tutorial.CssSettings._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object App {
  @JSImport("bootstrap/dist/css/bootstrap.css", JSImport.Namespace)
  @js.native
  object BootstrapCss extends js.Object

  @JSImport("assets/img/logo.svg", JSImport.Namespace)
  @js.native
  object Logo extends js.Object

  val Header = header(
    img(src := Logo.asInstanceOf[String]),
    width := 300.px,
    padding := 25.px
  )

  def main(args: Array[String]): Unit = {
    /* touch to include in build */
    BootstrapCss

    /* outputs all the styles */
    dom.document.head.appendChild(Styles.render[TypedTag[HTMLStyleElement]].render)

    /* render logo */
    dom.document.body.appendChild(Header.render)

    /* connect FileBrowser to where we want to render it in the DOM */
    val domTarget = div().render
    dom.document.body.appendChild(domTarget)

    /* tells `FileBrowser` how to update the dom */
    def updateDom(content: TypedTag[HTMLElement]): Unit = {
      domTarget.innerHTML = ""
      domTarget.appendChild(content.render)
    }

    new FileBrowser(
      remoteFetchPaths = path => AutowireClient[Api].fetchPathsUnder(path).call(),
      updateDom
    )
  }
}
