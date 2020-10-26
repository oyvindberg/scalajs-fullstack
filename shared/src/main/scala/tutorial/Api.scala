package tutorial

/** The shared API for the application
  */
trait Api {
  def fetchPathsUnder(dir: PathRef.DirectoryLike): LookupResult
}
