package tutorial

/**
  * The shared API for the application
  */
trait Api {
  def fetchPathsUnder(dir: PathRef): Either[LookupError, Seq[PathRef]]
}

sealed trait PathRef {
  def name: String
  override final def toString: String = {
    def go(loc: PathRef): List[String] =
      loc match {
        case DirRef(parent, name)  ⇒ name :: go(parent)
        case FileRef(parent, name) ⇒ name :: go(parent)
        case RootRef               ⇒ Nil
      }

    go(this).reverse.mkString("/", "/", "")
  }

  def parentOpt: Option[PathRef] =
    this match {
      case DirRef(parent, _) => Some(parent)
      case FileRef(parent, _) => Some(parent)
      case RootRef => None
    }
}

final case class DirRef(parent:  PathRef, name: String) extends PathRef
final case class FileRef(parent: PathRef, name: String) extends PathRef
case object RootRef extends PathRef {
  val name = "/"
}

sealed trait LookupError
case object LookupNotFound extends LookupError
case object LookupAccessDenied extends LookupError
