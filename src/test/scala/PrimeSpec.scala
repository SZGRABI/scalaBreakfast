import org.scalatest._

class MainSpec extends WordSpec with Matchers {
  "Main" should {
    "return list of tuples (number, bool) for all candidate numbers" in {
      val primes = List(1, 2, 3, 4)
      assert(Problems.last(list) == magicnum)
    }
  }
