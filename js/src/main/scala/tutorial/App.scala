package tutorial

import autowire._
import org.scalajs.dom

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import org.scalajs.dom.raw.HTMLStyleElement

import CssSettings._
import scalacss.ScalatagsCss._

object App extends js.JSApp {

  /* Entry point */
  override def main(): Unit = {
    /* connect FileBrowser to where we want to render it in the DOM */
    /* outputs all the styles */
    dom.document.head.appendChild(
      Styles.render[TypedTag[HTMLStyleElement]].render
    )

    val browser = new FileBrowser(
        remoteFetchPaths = path ⇒ AutowireClient[Api].fetchPathsUnder(path).call()
      )
    dom.document.body.appendChild(browser.render)

    /* render root directory */
    browser.fetchPathsUnder(RootRef)
  }
}

final case class State(wantedPath: PathRef, result: Seq[PathRef])

final class FileBrowser(remoteFetchPaths: PathRef ⇒ Future[Seq[PathRef]]) {

  /**
    * We keep old states for caching and easy back navigation
    */
  private var stateStack: List[State] = Nil

  private val rootDom = div().render

  def render(): dom.Node = {
    rootDom
  }

  /* perform Ajax call and handle result */
  def fetchPathsUnder(path: PathRef): Unit = {
    remoteFetchPaths(path).onComplete{
      case Success(resTry) ⇒
        pushState(State(path, resTry))
      case Failure(throwable) ⇒
        renderAlert(AlertMode.danger,
          () => fetchPathsUnder(path),
          "Unexpected error: ",
          throwable.getMessage)
    }
  }

  def pushState(state: State): Unit = {
    stateStack = state :: stateStack
    renderFileList()
  }

  def popState(): Unit = {
    stateStack = stateStack match {
      case Nil => Nil
      case _ :: tail => tail
    }
    renderFileList()
  }

  private def renderAlert(mode: AlertMode, retry: () ⇒ Unit, xs: Modifier*): Tag =
    div(`class` := s"alert $mode", renderButton("Retry", retry), xs)

  private def renderFileList(): Unit = {
    val content = stateStack.headOption match {
      case Some(State(path, refs)) ⇒
        div(
          /* reference a style */
          Styles.myStyle,
          div(
            `class` := "panel-heading",
            h1("Currently browsing ", path.toString),
            div(
              `class` := "btn-toolbar",
              Option(renderButton("Back", () => popState())).filter(_ => stateStack.size >= 2),
              renderButton("Refresh", () => fetchPathsUnder(path))
            )
          ),
          div(
            `class` := "panel-body",
            div(
              `class` := "list-group",
              div(
                refs.collect {
                  case dir: DirRef ⇒
                    button(dir.name,
                      `type` := "button",
                      `class` := "list-group-item",
                      onclick := {() => fetchPathsUnder(dir)}
                  )
                },
                refs.collect {
                  case file: FileRef ⇒
                    span(
                      `class` := "list-group-item",
                      span(`class` := "glyphicon glyphicon-file"),
                      file.name
                    )
                }
              )
            )
          )
        )
      case None ⇒
        h2("Loading")
    }

    rootDom.innerHTML = ""
    rootDom.appendChild(content.render)
  }

  def renderButton(title: String, onClick: () ⇒ Unit): Tag =
    button(title, `type` := "button", `class` := "btn, btn-group", onclick := onClick)
}

/* This is an example of how to make traditional interfaces more palatable. */
@js.native
sealed trait AlertMode extends js.Any

object AlertMode {
  /* these casts are ok because javascript.*/
  val success = "alert-success".asInstanceOf[AlertMode]
  val info    = "alert-info".asInstanceOf[AlertMode]
  val danger  = "alert-danger".asInstanceOf[AlertMode]
  val warning = "alert-warning".asInstanceOf[AlertMode]
}
