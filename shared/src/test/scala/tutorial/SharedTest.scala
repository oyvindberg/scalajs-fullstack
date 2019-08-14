package tutorial

import utest._

object SharedTest extends TestSuite {
  override def tests =
    Tests {
      'works {
        assert(1 + 1 == 2)
      }
    }
}
