package tutorial

import autowire._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, HTMLScriptElement, HTMLStyleElement}
import tutorial.CssSettings._

import scala.scalajs.{js, LinkingInfo}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalacss.ScalatagsCss._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

object App extends js.JSApp {

  /* Entry point */
  override def main(): Unit = {

    if (LinkingInfo.developmentMode) {
      val script = dom.document.createElement("script").asInstanceOf[HTMLScriptElement]
      script.`type` = "text/javascript"
      script.src = "//localhost:12345/workbench.js"
      dom.document.head.appendChild(script)
    }

    /* outputs all the styles */
    dom.document.head.appendChild(
      Styles.render[TypedTag[HTMLStyleElement]].render
    )

    /* connect FileBrowser to where we want to render it in the DOM */
    val domTarget = div().render
    dom.document.body.appendChild(domTarget)

    /* tells `FileBrowser` how to update the dom */
    def updateDom(content: TypedTag[HTMLElement]): Unit = {
      domTarget.innerHTML = ""
      domTarget.appendChild(content.render)
    }

    new FileBrowser(
      remoteFetchPaths = path ⇒ AutowireClient[Api].fetchPathsUnder(path).call(),
      updateDom
    )
  }
}
