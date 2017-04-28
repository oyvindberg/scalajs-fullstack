package tutorial

import org.scalajs.dom
import utest._
import utest.framework.{Test, Tree}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scalatags.JsDom.all._

object FileBrowserTest extends TestSuite {

  val IgnoreLookups: Path ⇒ () ⇒ Unit =
    path ⇒ () ⇒ ()

  def tests: Tree[Test] =
    TestSuite {
      'RenderLoadingScreen {
        assert(Renderer(None, IgnoreLookups, None) == h2("Loading"))
      }

      'CanNavigateAndRenderCorrectly {
        var rendered: List[dom.Element] =
          Nil

        val SubDir: DirPath =
          DirPath(Root, "sub")

        val mockServer: Path ⇒ LookupResult = {
          case Root ⇒
            LookupOk(directories = Seq(SubDir), files = Seq.empty)
          case SubDir ⇒
            LookupOk(Seq.empty, Seq.empty)
          case other ⇒
            LookupAccessDenied
        }

        val browser: FileBrowser =
          new FileBrowser(
            remoteFetchPaths = mockServer andThen Future.successful,
            replaceDom       = elem ⇒ rendered = elem :: rendered
          )

        for {
          _ ← browser.fetchPathsUnder(Root)
          _ ← browser.fetchPathsUnder(SubDir)} {

          assert(browser.stateStack.size == 2)
          assert(rendered.size == 3)

          assert(rendered.head.outerHTML ==
            """<div class="Styles-myStyle panel panel-default"><div class="panel-heading"><h1>Currently browsing /sub</h1><div class="btn-toolbar"><button type="button" class="btn, btn-group">Back</button><button type="button" class="btn, btn-group">Refresh</button></div></div><div class="panel-body"><div class="list-group"><div></div></div></div></div>"""
          )
        }
      }
    }
}
