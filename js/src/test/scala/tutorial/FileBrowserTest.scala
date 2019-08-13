package tutorial

import org.scalajs.dom.raw.HTMLElement
import utest._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalatags.JsDom._
import scalatags.JsDom.all._

object FileBrowserTest extends TestSuite {

  val IgnoreLookups: PathRef ⇒ () => Unit =
    path ⇒ () => ()

  def tests =
    Tests {
      'RenderLoadingScreen {
        assert(FileBrowser.render(FileBrowser.Loading, IgnoreLookups) == h2("Loading"))
      }

      'CanNavigateAndRenderCorrectly {
        var rendered: List[TypedTag[HTMLElement]] =
          Nil

        val SubDir: DirRef =
          DirRef(RootRef, "sub")

        val mockServer: PathRef ⇒ Either[LookupError, Seq[PathRef]] = {
          case RootRef ⇒
            Right(Seq(SubDir))
          case SubDir ⇒
            Right(Seq.empty)
          case other ⇒
            Left(LookupAccessDenied)
        }

        val browser: FileBrowser =
          new FileBrowser(
            remoteFetchPaths = mockServer andThen Future.successful,
            updateDom = (elem: TypedTag[HTMLElement]) ⇒ rendered = elem :: rendered
          )
        browser.fetchPathsUnder(SubDir).foreach {
          case () =>
            val Loading = "<h2>Loading</h2>"
            assert(rendered.size == 4)
            assert(rendered.filterNot(_.toString() == Loading).size == 2)}
      }
    }
}
