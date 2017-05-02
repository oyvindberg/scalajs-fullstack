package tutorial

import java.time.Instant

import upickle.Js.Str
import upickle.default

/**
  * These needs to be in scope to use `Instant` with upickle (json) and autowire
  */
object InstantCodec {
  implicit val InstantReader: default.Reader[Instant] =
    upickle.default.makeReader{
      case Str(str) => Instant.ofEpochMilli(str.toLong)
    }

  implicit val InstantWriter: default.Writer[Instant] =
    upickle.default.makeWriter((i: Instant) => Str(i.toEpochMilli.toString))
}
