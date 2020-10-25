package tutorial

import java.time.Instant

import upickle.default.{Reader, Writer}

/** These needs to be in scope to use `Instant` with upickle (json) and autowire
  */
object InstantCodec {
  implicit val InstantReader: Reader[Instant] =
    implicitly[Reader[Long]].map(Instant.ofEpochMilli)

  implicit val InstantWriter: Writer[Instant] =
    implicitly[Writer[Long]].comap[Instant](_.toEpochMilli)
}
