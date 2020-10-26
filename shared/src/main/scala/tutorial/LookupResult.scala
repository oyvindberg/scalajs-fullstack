package tutorial

import upickle.default.{ReadWriter => RW, macroRW}

sealed trait LookupResult

object LookupResult {
  sealed trait LookupError extends LookupResult
  case object NotFound extends LookupError
  case object AccessDenied extends LookupError
  case class Ok(contents: Seq[PathRef.NotRoot]) extends LookupResult

  implicit val rwLookupNotFound: RW[NotFound.type] = macroRW
  implicit val rwLookupAccessDenied: RW[AccessDenied.type] = macroRW
  implicit val rwLookupError: RW[LookupError] = macroRW
  implicit val rwOk: RW[Ok] = macroRW
  implicit val rwLookupResult: RW[LookupResult] = macroRW
}
