package tutorial

import java.io.File
import java.nio.file.Files
import java.time.Instant

case class ApiImpl(sandbox: File) extends Api {

  override def fetchPathsUnder(path: PathRef.DirectoryLike): LookupResult =
    parsePathToFile(path) match {
      case Left(notFound) =>
        notFound

      //lets pretend this is good enough
      case Right(file) if file.getAbsolutePath.startsWith(sandbox.getAbsolutePath) =>
        val filesUnder: Seq[File] =
          Option(file.list()).toSeq.flatten.map(new File(file, _))

        LookupResult.Ok(
          filesUnder
            .collect {
              case f if f.isDirectory => PathRef.Directory(path, f.getName)
              case f if f.isFile      => PathRef.File(path, f.getName)
            }
            .sortBy(f => (f.isInstanceOf[PathRef.File], f.name))
        )

      case outsideSandbox =>
        LookupResult.AccessDenied
    }

  def lastModified(f: File): Instant =
    Instant.ofEpochMilli(f.lastModified())

  def readFile(file: File): String =
    new String(Files.readAllBytes(file.toPath), "UTF-8")

  def parsePathToFile(loc: PathRef): Either[LookupResult.NotFound.type, File] =
    loc match {
      case PathRef.RootRef =>
        Right(sandbox)
      case PathRef.File(parent, name) =>
        existingFile(parent, name)
      case PathRef.Directory(parent, name) =>
        existingFile(parent, name)
    }

  def existingFile(parent: PathRef, name: String): Either[LookupResult.NotFound.type, File] =
    parsePathToFile(parent).flatMap { parentFile =>
      new File(parentFile, name) match {
        case f if f.exists =>
          Right(f)
        case _ =>
          Left(LookupResult.NotFound)
      }
    }
}
