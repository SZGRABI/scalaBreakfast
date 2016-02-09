import java.io.File
import java.math.BigInteger
import java.util.Date

import akka.actor.ActorSystem
import akka.stream.io.{Framing, IOResult}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import akka.util.ByteString

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random
;

/**
 * @author gszabo<gabor.szabo@webvalto.hu>
 */
// Object : statikus , ha class és object is van akkor companion osztály
// Streams : Majdnem mint javaban, csak ott blokkoló.
// Itt non blocking .Threadpool felett megy.
object Main {

  // Sok segitő függvény jár mellé.
  case class PrimalityResult(n: BigInteger, isPrime: Boolean)


  def main(args: Array[String]) {
    implicit val system = ActorSystem("stream-demo")
    // implicit :scoponként 1 egy tipusbol, fvbe lehet praméter kicserélve rá, nem kell beirni.
    implicit val mat = ActorMaterializer()

    try {
      //      val src: Source[Int, NotUsed] = Source(1 to 10);
      //      val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ + 1);
      //      val sink: Sink[Int, Future[Done]] = Sink.foreach[Int]{x=> println(x)};
      //      val graph: RunnableGraph[NotUsed] = src.via(flow).to(sink);
      //      graph.run();

      val fileSource: Source[ByteString, Future[IOResult]] = FileIO.fromFile(new File("test.txt"))
      val fileRawLines = fileSource.via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256))
      val fileLines: Source[String, Future[IOResult]] = fileRawLines.map(_.utf8String)
      val numbers = fileLines.map(text => new BigInteger(text))
      //      numbers.runForeach(println)

      val primeTester = Flow[BigInteger].map {
        n => PrimalityResult(n, n.isProbablePrime(100))
      }

      val randomNumbers = Source.fromIterator(() => new Iterator[Int] {
        override def hasNext: Boolean = true

        override def next(): Int = Random.nextInt(Int.MaxValue);
      })

      val primeTesterAsync = Flow[BigInteger].viaAsync(primeTester);

      val parPrimeTester = GraphDSL.create() { implicit builder =>

        import GraphDSL.Implicits._

        val balancer = builder.add(Balance[BigInteger](4))
        val merge = builder.add(Merge[PrimalityResult](4))
        val tester1 = builder.add(primeTesterAsync)
        val tester2 = builder.add(primeTesterAsync)

        balancer.out(0) ~> tester1 ~> merge.in(0)
        balancer.out(1) ~> tester2 ~> merge.in(1)

        FlowShape(balancer.in, merge.out)
      }

      println(new Date)
      Source(1 to Int.MaxValue)
        .map(BigInteger.valueOf(_))
        .via(primeTester)
        .filter(_.isPrime)
        .take(30000)
        .runForeach(println)
        .onSuccess{case _ => println(new Date)}(system.dispatcher)

      //      FileIO.fromFile(new File("test.txt")).via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 255)).map(_.ut8String))

      readLine();
    } finally {
      Await.result(system.terminate(), 3.second)
    }
  }
}
