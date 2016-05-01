package tutorial

import autowire._
import org.scalajs.dom

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import Capture.instances._

object App extends js.JSApp {

  /* Entry point */
  override def main(): Unit = {
    /* connect FileBrowser to where we want to render it in the DOM */
    val target: dom.html.Div =
      div().render

    dom.document.body.appendChild(target)

    val browser: FileBrowser =
      new FileBrowser(
        remoteFetchPaths =
          path ⇒ AutowireClient[Api].fetchPathsUnder(path).call(),

        replaceDom = (elem: dom.Element) ⇒ {
          target.innerHTML = ""
          target.appendChild(elem)
        }
      )

    /* render root directory */
    browser.fetchPathsUnder(Root)
  }
}

final case class State(wantedPath: Path, result: Try[LookupResult])

final class FileBrowser(remoteFetchPaths: Path ⇒ Future[LookupResult],
                        replaceDom:       dom.Element ⇒ Unit) {

  /**
    * We keep old states for caching and easy back navigation
    */
  var stateStack: List[State] =
    Nil

  /* initial rendering */
  render()

  def render(): Unit = {
    val renderedContent: TypedTag[dom.Element] =
      Renderer(
        stateOpt        = stateStack.headOption,
        fetchPathsUnder = path ⇒ () ⇒ {fetchPathsUnder(path); ()},
        backOpt         = Some(popState _).filter(_ ⇒ stateStack.size > 1)
      )

    val domContent: dom.Element =
      renderedContent.render

    replaceDom(domContent)
  }

  /* perform Ajax call and handle result */
  def fetchPathsUnder(path: Path): Future[Unit] = {
    val resultF: Future[LookupResult] =
      remoteFetchPaths(path)

    val recoverFT: Future[Try[LookupResult]] =
      resultF.map(Success.apply).recover {
        case NonFatal(th) ⇒ Failure(th)
      }

    recoverFT.map(resTry ⇒ pushState(State(path, resTry)))
  }

  def pushState(state: State): Unit = {
    stateStack = state :: stateStack.dropWhile(old ⇒ old.result.isFailure || old == state)
    render()
  }

  def popState(): Unit = {
    stateStack = stateStack.tail
    render()
  }
}

object Renderer {
  /**
    * Render current state without side-effects
    *
    * @param stateOpt current state, if any
    * @param fetchPathsUnder callback for fetching the subpaths under a path
    * @param backOpt optional callback for returning to previous state
    * @return dom element to be rendered
    */
  def apply(stateOpt:        Option[State],
            fetchPathsUnder: Path ⇒ () ⇒ Unit,
            backOpt:         Option[() ⇒ Unit]): TypedTag[dom.Element] =

    stateOpt match {
      case Some(State(path, res)) ⇒
        div(
          `class` := "panel panel-default",
          div(
            `class` := "panel-heading",
            h1("Currently browsing ", path.toString),
            div(
              `class` := "btn-toolbar",
              backOpt.map(back ⇒ renderButton("Back", back)),
              renderButton("Refresh", fetchPathsUnder(path))
            )
          ),
          div(
            `class` := "panel-body",
            res match {
              case Success(LookupOk(dirs, files)) ⇒
                div(
                  `class` := "list-group",
                  div(
                    dirs map (dir ⇒
                      button(dir.name,
                        `type` := "button",
                        `class` := "list-group-item",
                        onclick := fetchPathsUnder(dir)
                      )
                    )
                  ),
                  files map (file ⇒
                    span(
                      `class` := "list-group-item",
                      span(`class` := "glyphicon glyphicon-file"),
                      file.name
                    )
                  )
                )

              case Success(LookupAccessDenied) ⇒
                renderAlert(AlertMode.warning, fetchPathsUnder(path), "Access denied")

              case Success(LookupNotFound(name)) ⇒
                renderAlert(AlertMode.warning, fetchPathsUnder(path), s"Path $name not found")

              case Failure(throwable) ⇒
                renderAlert(AlertMode.danger, fetchPathsUnder(path), "Unexpected error: ", throwable.getMessage)
            }
          )
        )
      case None ⇒
        h2("Loading")
    }

  def renderAlert(mode: AlertMode, retry: () ⇒ Unit, xs: Modifier*): TypedTag[dom.html.Div] =
    div(`class` := s"alert $mode", renderButton("Retry", retry), xs)

  def renderButton(title: String, onClick: () ⇒ Unit): TypedTag[dom.html.Button] =
    button(title, `type`  := "button", `class` := "btn, btn-group", onclick := onClick)
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