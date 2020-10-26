package tutorial

import slinky.core.FunctionalComponent
import slinky.core.annotations.react
import slinky.core.facade.Hooks
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg.fileOutlinedMod.{default => FileOutlineIcon}
import typings.antDesignIconsSvg.folderOutlinedMod.{default => FolderOutlineIcon}
import typings.antd.listMod.ListItemLayout
import typings.antd.{antdStrings, components => antd}
import typings.react.components._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

@react
object FileBrowser {
  case class Props(remoteFetchPaths: PathRef.DirectoryLike => Future[LookupResult])

  sealed trait State {
    def pathOpt: Option[PathRef] =
      this match {
        case State.AtDir(path, _) => Some(path)
        case State.Error(_, _)    => None
        case State.Loading        => None
      }
  }

  object State {
    case object Loading extends State
    case class AtDir(dir: PathRef.DirectoryLike, contents: js.Array[PathRef.NotRoot]) extends State
    case class Error(msg: String, retry: () => Unit) extends State
  }

  val component = FunctionalComponent[Props] { props =>
    val (state, setState) = Hooks.useState[State](State.Loading)

    def fetch(wantedPath: PathRef.DirectoryLike): Future[Unit] = {
      setState(State.Loading)

      props.remoteFetchPaths(wantedPath).transform { result =>
        def error(msg: String): State.Error =
          State.Error(msg, () => fetch(wantedPath))

        val nextState = result match {
          case Success(LookupResult.Ok(dirContents)) =>
            State.AtDir(wantedPath, dirContents.toJSArray)

          case Success(LookupResult.NotFound) =>
            error(s"$wantedPath was not found")

          case Success(LookupResult.AccessDenied) =>
            error(s"Access denied to $wantedPath")

          case Failure(throwable) =>
            error(s"Unexpected error: ${throwable.getMessage}")
        }

        Success(setState(nextState))
      }
    }

    // initial load
    Hooks.useEffect(() => fetch(PathRef.RootRef), List())

    state match {
      case State.AtDir(dir, contents) =>
        div(
          antd.Typography.Title("Currently browsing", dir.toString),
          state.pathOpt.flatMap(_.parentOpt).map { parent =>
            antd.Button.onClick(_ => fetch(parent))("Back")
          },
          antd.Button.onClick(_ => fetch(dir))("Refresh"),
          antd
            .List()
            .dataSource(contents)
            .itemLayout(ListItemLayout.horizontal)
            .renderItem {
              case (dir @ PathRef.Directory(_, dirName), _) =>
                val meta = antd.List.Item.Meta
                  .title(antd.Typography.Link(dirName))
                  .avatar(AntdIcon(FolderOutlineIcon))

                antd.List.Item.withKey(dirName)(meta).onClick(_ => fetch(dir))
              case (PathRef.File(_, fileName), _) =>
                val meta = antd.List.Item.Meta
                  .title(antd.Typography.Text(fileName))
                  .avatar(AntdIcon(FileOutlineIcon))

                antd.List.Item.withKey(fileName)(meta)
            }
        )

      case State.Loading =>
        antd.Typography.Title("Loading")

      case State.Error(msg, retry) =>
        antd.Alert
          .`type`(antdStrings.warning)
          .onClick(_ => retry())
          .message(msg)
    }
  }
}
