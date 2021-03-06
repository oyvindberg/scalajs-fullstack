package tutorial

import upickle.default.{ReadWriter => RW, macroRW}

sealed trait PathRef {
  def name: String

  override final def toString: String = {
    def go(loc: PathRef): List[String] =
      loc match {
        case d: PathRef.Directory => loc.name :: go(d.parent)
        case f: PathRef.File      => loc.name :: go(f.parent)
        case PathRef.RootRef => Nil
      }

    go(this).reverse.mkString("/", "/", "")
  }

  def parentOpt: Option[PathRef.DirectoryLike] =
    this match {
      case d: PathRef.Directory => Some(d.parent)
      case f: PathRef.File      => Some(f.parent)
      case PathRef.RootRef => None
    }
}


object PathRef {
  implicit val rwFileRef:    RW[File]          = macroRW
  implicit val rwDirRef:     RW[Directory]     = macroRW
  implicit val rwDirPathRef: RW[DirectoryLike] = macroRW
  implicit val rwNotRoot:    RW[NotRoot]       = macroRW
  implicit val rwPathRef:    RW[PathRef]       = macroRW

  sealed trait DirectoryLike extends PathRef
  sealed trait NotRoot extends PathRef

  final case class Directory(parent: DirectoryLike, name: String) extends DirectoryLike with NotRoot

  final case class File(parent: DirectoryLike, name: String) extends PathRef with NotRoot

  case object RootRef extends DirectoryLike {
    val name = "/"
  }
}
