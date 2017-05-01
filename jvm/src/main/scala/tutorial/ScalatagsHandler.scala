package tutorial

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`text/html`

import scalatags.Text.Frag

object ScalatagsHandler extends ScalatagsHandler

trait ScalatagsHandler {

  private val scalatagsMarshaller = Marshaller.stringMarshaller(`text/html`)

  implicit def scalatagsToEntityMarshaller[F <: Frag]: ToEntityMarshaller[F] =
    scalatagsMarshaller.compose((f: F) => f.render)
}
