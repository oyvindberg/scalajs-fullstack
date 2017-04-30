package tutorial

import java.io.File

case class ApiImpl(sandbox: File) extends Api {

  override def fetchPathsUnder(path: PathRef): Seq[PathRef] =
    parsePathToFile(path) match {
      case None ⇒
        Nil

      //lets pretend this is good enough
      case Some(file) if file.getAbsolutePath.startsWith(sandbox.getAbsolutePath) ⇒
        val filesUnder: Seq[File] =
          Option(file.list()).toSeq.flatten.map(new File(file, _))

        filesUnder.map {
          case f if f.isDirectory ⇒ DirRef(path, f.getName)
          case f if f.isFile ⇒ FileRef(path, f.getName)
        }.sortBy(_.name)

      case outsideSandbox ⇒
        Nil
    }

  def parsePathToFile(loc: PathRef): Option[File] =
    loc match {
      case RootRef ⇒
        Some(sandbox)
      case FileRef(parent, name) ⇒
        existingFile(parent, name)
      case DirRef(parent, name) ⇒
        existingFile(parent, name)
    }

  def existingFile(parent: PathRef, name: String): Option[File] =
    parsePathToFile(parent).flatMap { parentFile ⇒
      new File(parentFile, name) match {
        case f if f.exists ⇒
          Some(f)
        case _ ⇒
          None
      }
    }
}
