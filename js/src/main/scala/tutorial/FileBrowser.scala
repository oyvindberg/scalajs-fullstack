package tutorial

import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scalacss.ScalatagsCss._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

final class FileBrowser(remoteFetchPaths: DirPathRef ⇒ Future[Either[LookupError, Seq[PathRef]]],
                        remoteFetchFile:  FileRef ⇒ Future[Either[LookupError, String]],
                        updateDom:        TypedTag[HTMLElement] => Unit) {

  def setState(state: FileBrowser.State): Unit =
    updateDom(
      FileBrowser.render(
        state,
        path => () => fetchPathsUnder(path),
        file => () => fetchFile(file)
      ))

  def fetchPathsUnder(wantedPath: DirPathRef): Future[Unit] = {
    setState(FileBrowser.Loading)

    remoteFetchPaths(wantedPath).transform { (result: Try[Either[LookupError, Seq[PathRef]]]) =>
      val state: FileBrowser.State =
        newState(wantedPath, result, () => fetchPathsUnder(wantedPath))(dirContents =>
          FileBrowser.AtDir(wantedPath, dirContents))

      Success(setState(state))
    }
  }

  def fetchFile(wantedFile: FileRef): Future[Unit] = {
    setState(FileBrowser.Loading)

    remoteFetchFile(wantedFile).transform { (result: Try[Either[LookupError, String]]) =>
      val state: FileBrowser.State =
        newState(wantedFile, result, () => remoteFetchFile(wantedFile))(dirContents =>
          FileBrowser.AtFile(wantedFile, dirContents))

      Success(setState(state))
    }
  }

  def newState[T](wantedPath:  PathRef,
                  result:      Try[Either[LookupError, T]],
                  retryAction: () => Unit)(success: T => FileBrowser.State): FileBrowser.State = {

    def error(msg: String): FileBrowser.State =
      FileBrowser.Error(msg, retryAction)

    result match {
      case Success(Right(t)) ⇒
        success(t)

      case Success(Left(LookupNotFound)) =>
        error(s"$wantedPath was not found")

      case Success(Left(LookupAccessDenied)) =>
        error(s"Access denied to $wantedPath")

      case Success(Left(LookupTooBig)) =>
        error(s"$wantedPath was too big")

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
        case AtDir(path, _)  => Some(path)
        case AtFile(file, _) => Some(file)
        case Error(_, _)     => None
        case Loading         => None
      }
  }

  case object Loading extends State
  case class AtDir(path:  DirPathRef, dirContents: Seq[PathRef]) extends State
  case class AtFile(file: FileRef, contents:       String)       extends State
  case class Error(msg:   String, retry:           () => Unit)   extends State

  def render(state:     FileBrowser.State,
             fetchDir:  DirPathRef => () => Unit,
             fetchFile: FileRef => () => Unit): TypedTag[HTMLElement] =
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
              state.pathOpt.flatMap(_.parentOpt).map(parent => Bootstrap.btn("Back", fetchDir(parent))),
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
                  case file @ FileRef(parent, fileName, size, lastModified) ⇒
                    a(
                      `class` := "list-group-item",
                      span(`class` := "glyphicon glyphicon-file"),
                      fileName,
                      cursor := "pointer",
                      span(`class` := "badge", s"${size / 1024} kb, last modified: $lastModified"),
                      onclick := fetchFile(file)
                    )
                }
              )
            )
          )
        )

      case FileBrowser.AtFile(file, contents) ⇒
        div(
          /* reference a scalacss style */
          Styles.myStyle,
          div(
            `class` := "panel-heading",
            h1("Currently browsing", file.toString),
            div(
              `class` := "btn-toolbar",
              state.pathOpt.flatMap(_.parentOpt).map(parent => Bootstrap.btn("Back", fetchDir(parent))),
              Bootstrap.btn("Refresh", fetchFile(file))
            )
          ),
          code(pre(contents))
        )

      case FileBrowser.Loading ⇒
        h2("Loading")

      case FileBrowser.Error(msg, retry) =>
        Bootstrap.alert(AlertMode.danger, retry, msg)

    }
}
