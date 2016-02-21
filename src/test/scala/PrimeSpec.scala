// import org.scalatest._
import akka.testkit.{TestKit, TestActorRef}
import akka.stream.testkit._

// http://doc.akka.io/docs/akka/2.4.2/scala/stream/stream-testkit.html
// http://blog.matthieuguillermin.fr/2013/06/akka-testing-your-actors/
val primes = List(3)
val result = Prime.PrimalityResult(3, true)
val graph = Source(3).runWith(genGraph(primes, 2)
assert(graph == result)
// class PrimeTest extends TestKit(ActorSystem("testSystem"))
//   with WordSpec
//   with MustMatchers {

//   "Prime actor " must {

//     val 
//   }
// }
