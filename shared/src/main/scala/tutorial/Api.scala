package tutorial

import upickle.default._

/**
  * The shared API for the application
  */
trait Api{
  def fetchPathsUnder(dir: Path): LookupResult
}

sealed trait Path {
  override final def toString: String = {
    def go(loc: Path): List[String] =
      loc match {
        case DirPath(parent, name)  ⇒ name :: go(parent)
        case FilePath(parent, name) ⇒ name :: go(parent)
        case Root                   ⇒ Nil
      }

    go(this).reverse.mkString("/", "/", "")
  }
}

final case class DirPath(parent: Path, name: String) extends Path
final case class FilePath(parent: Path, name: String) extends Path
case object Root extends Path

sealed trait LookupResult
final case class LookupOk(directories: Seq[DirPath], files: Seq[FilePath]) extends LookupResult
final case class LookupNotFound(name: String) extends LookupResult
case object LookupAccessDenied extends LookupResult

case class ErrorMsg(value: String) extends AnyVal

/**
  * Hack alert. Because of a compiler bug (SI-7046),
  *  the compiler is not able to figure out all subtypes
  *  of a sealed class hierarchy in some cases.
  *
  * One of those cases is the Scala.js pattern of sharing code in
  *  a `shared/` folder.
  *
  *  The workaround is to make sure the implicits are derived
  *   in code within the `shared/` folder itself, so we capture them here.
  */
class Capture(implicit val pathReader:   Reader[Path],
                       val pathWriter:   Writer[Path],
                       val resultReader: Reader[LookupResult],
                       val resultWriter: Writer[LookupResult])

object Capture {
  val instances: Capture =
    new Capture()(macroR[Path], macroW[Path], macroR[LookupResult], macroW[LookupResult])
}
