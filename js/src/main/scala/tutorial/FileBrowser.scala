package tutorial

import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scalacss.ScalatagsCss._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

final class FileBrowser(remoteFetchPaths: PathRef ⇒ Future[Either[LookupError, Seq[PathRef]]],
                        updateDom:        TypedTag[HTMLElement] => Unit) {

  def setState(state: FileBrowser.State): Unit =
    updateDom(FileBrowser.render(state, fetchPathsUnderCallback))

  def fetchPathsUnder(wantedPath: PathRef): Future[Unit] = {
    setState(FileBrowser.Loading)

    remoteFetchPaths(wantedPath).transform { (result: Try[Either[LookupError, Seq[PathRef]]]) =>
      Success(setState(newState(wantedPath, result)))
    }
  }

  def fetchPathsUnderCallback(wantedPath: PathRef): () => Unit =
    () => fetchPathsUnder(wantedPath)

  def newState(wantedPath: PathRef,
               result:     Try[Either[LookupError, Seq[PathRef]]]): FileBrowser.State = {

    def error(msg: String): FileBrowser.State =
      FileBrowser.Error(msg, fetchPathsUnderCallback(wantedPath))

    result match {
      case Success(Right(dirContents)) ⇒
        FileBrowser.AtDir(wantedPath, dirContents)

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
        case _              => None
      }
  }

  case object Loading extends State

  case class AtDir(path: PathRef, dirContents: Seq[PathRef]) extends State

  case class Error(msg: String, retry: () => Unit) extends State

  def render(state: FileBrowser.State, fetchDir: PathRef => () => Unit): TypedTag[HTMLElement] =
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
                  case dir: DirRef ⇒
                    button(dir.name,
                           `type` := "button",
                           `class` := "list-group-item",
                           onclick := fetchDir(dir))
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

      case FileBrowser.Loading ⇒
        h2("Loading")

      case FileBrowser.Error(msg, retry) =>
        Bootstrap.alert(AlertMode.danger, retry, msg)
    }

}
