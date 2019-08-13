package tutorial

import org.scalajs.dom.html.{Button, Div}

import scala.scalajs.js
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

/* just as much of bootstrap as we needed */
object Bootstrap {
  def alert(mode: AlertMode, retry: () => Unit, xs: Modifier*): TypedTag[Div] =
    div(`class` := s"alert $mode", btn("Retry", retry), xs)

  def btn(title: String, onClick: () => Unit): TypedTag[Button] =
    button(title, `type` := "button", `class` := "btn, btn-group", onclick := onClick)
}

/* This is an example of how to make traditional interfaces more palatable. */
@js.native
sealed trait AlertMode extends js.Any

object AlertMode {
  /* these casts are ok because javascript.*/
  val success = "alert-success".asInstanceOf[AlertMode]
  val info    = "alert-info".asInstanceOf[AlertMode]
  val danger  = "alert-danger".asInstanceOf[AlertMode]
  val warning = "alert-warning".asInstanceOf[AlertMode]
}
