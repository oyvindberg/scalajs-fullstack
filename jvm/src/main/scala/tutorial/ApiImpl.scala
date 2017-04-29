package tutorial

import java.io.File

case class ApiImpl(sandbox: File) extends Api {

  override def fetchPathsUnder(path: PathRef): Either[LookupError, Seq[PathRef]] =
    parsePathToFile(path) match {
      case Left(notFound) ⇒
        Left(notFound)

      //lets pretend this is good enough
      case Right(file) if file.getAbsolutePath.startsWith(sandbox.getAbsolutePath) ⇒
        val filesUnder: Seq[File] =
          Option(file.list()).toSeq.flatten.map(new File(file, _))

        Right(filesUnder.map {
          case f if f.isDirectory ⇒ DirRef(path, f.getName)
          case f if f.isFile ⇒ FileRef(path, f.getName)
        }.sortBy(_.name))

      case outsideSandbox ⇒
        Left(LookupAccessDenied)
    }

  def parsePathToFile(loc: PathRef): Either[LookupNotFound, File] =
    loc match {
      case RootRef ⇒
        Right(sandbox)
      case FileRef(parent, name) ⇒
        existingFile(parent, name)
      case DirRef(parent, name) ⇒
        existingFile(parent, name)
    }

  def existingFile(parent: PathRef, name: String): Either[LookupNotFound, File] =
    parsePathToFile(parent).right.flatMap { parentFile ⇒
      new File(parentFile, name) match {
        case f if f.exists ⇒
          Right(f)
        case _ ⇒
          Left(LookupNotFound(name))
      }
    }
}
