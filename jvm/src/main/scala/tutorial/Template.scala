package tutorial

import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.Text.tags2.title

/**
  * This uses scalatags on the serverside, which as we can see
  *  renders as `String`, because we import from `scalatags.Text`.
  */
object Template {
  def scriptTag(s: String): Frag =
    script(`type` := "text/javascript", src := s"/js/$s")

  val asScalaTags: TypedTag[String] =
    html(
      head(
        title("Example Scala.js application"),
        meta(
          httpEquiv := "Content-Type",
          content := "text/html; charset=UTF-8"
        ),
        scriptTag("scala-js-workshop-jsdeps.js"),
        link(
          rel := "stylesheet",
          tpe := "text/css",
          href := "META-INF/resources/webjars/bootstrap/3.3.5/css/bootstrap.min.css"
        )
      ),
      body(
        header(
          img(src := "img/logo.svg"),
          width := 300.px,
          padding := 25.px
        )
      ),
      scriptTag("scala-js-workshop-opt.js")
    )

  val asText: String =
    "<!DOCTYPE html>" + asScalaTags
}
