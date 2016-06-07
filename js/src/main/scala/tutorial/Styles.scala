package tutorial

import scala.language.postfixOps
import scalacss.Defaults._
import scalacss.StyleA

/* Scalacss example */
object Styles extends StyleSheet.Inline {
  import dsl._

  /* mixin - doesnt get a classname */
  val common = mixin(
    backgroundColor.mediumaquamarine
  )

  val myStyle = style(
    /* integrate with bootstrap */
    addClassName("panel"),
    addClassName("panel-default"),
    /* Applying our mixin */
    common,
    /* add some more properties */
    margin(20.px),
    cursor.allScroll,

    &.hover(
      cursor.move
    )
  )

  /** Style requiring an Int when applied. */
  val indent: (Int) ⇒ StyleA =
    styleF.int(0 to 3)(i => styleS(
      paddingLeft(i * 2.ex)
    ))
}