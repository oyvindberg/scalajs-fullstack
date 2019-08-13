package tutorial

import upickle.default.{ReadWriter => RW, macroRW}

/**
  * The shared API for the application
  */
trait Api {
  def fetchPathsUnder(dir: DirPathRef): Either[LookupError, Seq[PathRef]]

  //  def fetchFile(file: FileRef): Either[LookupError, String]
}

sealed trait PathRef {
  def name: String

  override final def toString: String = {
    def go(loc: PathRef): List[String] =
      loc match {
        case d: DirRef  => loc.name :: go(d.parent)
        case f: FileRef => loc.name :: go(f.parent)
        case RootRef => Nil
      }

    go(this).reverse.mkString("/", "/", "")
  }

  def parentOpt: Option[DirPathRef] =
    this match {
      case d: DirRef  => Some(d.parent)
      case f: FileRef => Some(f.parent)
      case RootRef => None
    }
}

object PathRef {
  implicit val rwFileRef:    RW[FileRef]    = macroRW
  implicit val rwDirRef:     RW[DirRef]     = macroRW
  implicit val rwDirPathRef: RW[DirPathRef] = macroRW
  implicit val rwPathRef:    RW[PathRef]    = macroRW
}

sealed trait DirPathRef extends PathRef

final case class DirRef(parent: DirPathRef, name: String) extends DirPathRef

final case class FileRef(parent: DirPathRef, name: String) extends PathRef

case object RootRef extends DirPathRef {
  val name = "/"
}

sealed trait LookupError
case object LookupNotFound extends LookupError
case object LookupAccessDenied extends LookupError

object LookupError {
  implicit val rwPathRef: RW[LookupError] = macroRW
}
