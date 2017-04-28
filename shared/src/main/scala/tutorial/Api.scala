package tutorial

/**
  * The shared API for the application
  */
trait Api {
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

final case class DirPath(parent:  Path, name: String) extends Path
final case class FilePath(parent: Path, name: String) extends Path
case object Root extends Path

sealed trait LookupResult
final case class LookupOk(directories: Seq[DirPath], files: Seq[FilePath]) extends LookupResult
final case class LookupNotFound(name:  String) extends LookupResult
case object LookupAccessDenied extends LookupResult

case class ErrorMsg(value: String) extends AnyVal
