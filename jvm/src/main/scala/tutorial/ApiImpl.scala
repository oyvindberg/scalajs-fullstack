package tutorial

import java.io.File

case class ApiImpl(sandbox: File) extends Api {

  override def fetchPathsUnder(path: Path): LookupResult =
    parsePathToFile(path) match {
      case Left(notFound) ⇒
        notFound
      //lets pretend this is good enough
      case Right(file) if file.getAbsolutePath.startsWith(sandbox.getAbsolutePath) ⇒
        val filesUnder: Seq[File] =
          Option(file.list()).toSeq.flatten.map(new File(file, _))

        LookupOk(
          directories = filesUnder
            .collect {
              case f if f.isDirectory ⇒ DirPath(path, f.getName)
            }
            .sortBy(_.name),
          files = filesUnder
            .collect {
              case f if f.isFile ⇒ FilePath(path, f.getName)
            }
            .sortBy(_.name)
        )

      case outsideSandbox ⇒
        LookupAccessDenied
    }

  def parsePathToFile(loc: Path): Either[LookupNotFound, File] =
    loc match {
      case Root ⇒
        Right(sandbox)
      case FilePath(parent, name) ⇒
        existingFile(parent, name)
      case DirPath(parent, name) ⇒
        existingFile(parent, name)
    }

  def existingFile(parent: Path, name: String): Either[LookupNotFound, File] =
    parsePathToFile(parent).right.flatMap { parentFile ⇒
      new File(parentFile, name) match {
        case f if f.exists ⇒
          Right(f)
        case _ ⇒
          Left(LookupNotFound(name))
      }
    }
}
