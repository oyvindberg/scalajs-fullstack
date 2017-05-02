package tutorial

import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scalacss.ScalatagsCss._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

final class FileBrowser(remoteFetchPaths: DirPathRef ⇒ Future[Either[LookupError, Seq[PathRef]]],
                        updateDom:        TypedTag[HTMLElement] => Unit) {

  def setState(state: FileBrowser.State): Unit =
    updateDom(FileBrowser.render(state, path => () => fetchPathsUnder(path)))

  def fetchPathsUnder(wantedPath: DirPathRef): Future[Unit] = {
    setState(FileBrowser.Loading)

    remoteFetchPaths(wantedPath).transform { (result: Try[Either[LookupError, Seq[PathRef]]]) =>
      val state: FileBrowser.State =
        newState(wantedPath, result)(dirContents => FileBrowser.AtDir(wantedPath, dirContents))

      Success(setState(state))
    }
  }

  def newState[T](wantedPath: DirPathRef, result: Try[Either[LookupError, T]])(
      success:                T => FileBrowser.State): FileBrowser.State = {

    def error(msg: String): FileBrowser.State =
      FileBrowser.Error(msg, () => fetchPathsUnder(wantedPath))

    result match {
      case Success(Right(t)) ⇒
        success(t)

      case Success(Left(LookupNotFound)) =>
        error(s"$wantedPath was not found")

      case Success(Left(LookupAccessDenied)) =>
        error(s"Access denied to $wantedPath")

      case Failure(throwable) ⇒
        error(s"Unexpected error: ${throwable.getMessage}")
    }
  }

  /* initialize */
  fetchPathsUnder(RootRef)
}

object FileBrowser {
  sealed trait State {
    def pathOpt: Option[PathRef] =
      this match {
        case AtDir(path, _) => Some(path)
        case Error(_, _)    => None
        case Loading        => None
      }
  }

  case object Loading extends State
  case class AtDir(path: DirPathRef, dirContents: Seq[PathRef]) extends State
  case class Error(msg:  String, retry:        () => Unit)   extends State

  def render(state: FileBrowser.State, fetchDir: DirPathRef => () => Unit): TypedTag[HTMLElement] =
    state match {
      case FileBrowser.AtDir(path, refs) ⇒
        div(
          /* reference a scalacss style */
          Styles.myStyle,
          div(
            `class` := "panel-heading",
            h1("Currently browsing", path.toString),
            div(
              `class` := "btn-toolbar",
              state.pathOpt
                .flatMap(_.parentOpt)
                .map(parent => Bootstrap.btn("Back", fetchDir(parent))),
              Bootstrap.btn("Refresh", fetchDir(path))
            )
          ),
          div(
            `class` := "panel-body",
            div(
              `class` := "list-group",
              div(
                refs.collect {
                  case dir @ DirRef(parent, dirName) ⇒
                    button(dirName,
                           `type` := "button",
                           `class` := "list-group-item",
                           onclick := fetchDir(dir))
                },
                refs.collect {
                  case file @ FileRef(parent, fileName) ⇒
                    a(
                      `class` := "list-group-item",
                      span(`class` := "glyphicon glyphicon-file"),
                      fileName,
                      cursor := "pointer"
                    )
                }
              )
            )
          )
        )

      case FileBrowser.Loading ⇒
        h2("Loading")

      case FileBrowser.Error(msg, retry) =>
        Bootstrap.alert(AlertMode.danger, retry, msg)

    }
}
