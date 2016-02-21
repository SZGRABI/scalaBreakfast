// import java.math.BigInteger
// import java.util.Date
import java.io.File

import akka.actor.ActorSystem
import akka.stream.io._
import akka.stream.stage.{GraphStageLogic, GraphStage}
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.util.ByteString

import scala.io.Source._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random


/**
 * @author gszabo<gabor.szabo@webvalto.hu>
 * @modified danielberecz<daniel.berecz@gmail.com>
 */

class Prime {
  case class PrimalityResult(n: Int, isPrime: Boolean)

  // from: http://www.scala-lang.org/old/node/8179
  def isPrime(n: Int): PrimalityResult = {
    val primeBool = ((2 until n - 1) forall (n % _ != 0))
    return PrimalityResult(n, primeBool)
  }
}
object Prime extends Prime {

    implicit val system = ActorSystem("akka-prime")

    implicit val mat = ActorMaterializer()

    val fileName = "test.txt"

    // val primeTesterAsync = Flow[Int].viaAsync(primeTester);

    // Int.Value from: http://eng.localytics.com/akka-streams-akka-without-the-actors/
    // TODO: make file reading on graph and prime another and connect them
    // val fileSource = FileIO.fromFile(new File(fileName)
    //   ).via(Framing.delimiter(ByteString("\n"),
    //     maximumFrameLength = Int.MaxValue)
    //   ).map(_.utf8String.toInt)

    // test list
    // val numbers = List(1, 2, 3, 4, 5, 10, 11, 19, 25)

    // from: http://stackoverflow.com/questions/6557169/how-to-declare-empty-list-and-then-add-string-in-scala
  val numbers = (for (line <- scala.io.Source.fromFile(fileName).getLines)
    yield line.toInt).toList

  def genGraph(inputList: List[Int] , workerCount: Int) = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit b =>

    import GraphDSL.Implicits._

    val in = Source(inputList)
    val out = Sink.foreach(println)

    // make async
    val worker = Flow[Int].map(isPrime)

    // make generic type
    val balance = b.add(Balance[Int](workerCount))
    val merge   = b.add(Merge[PrimalityResult](workerCount))


    in ~>  balance.in
    for (i <- 0 until workerCount)
      balance.out(i) ~> worker ~> merge.in(i)
                                merge.out ~> out
    ClosedShape
  })

  def main(args: Array[String]) {

    try {

      genGraph(inputList = numbers, workerCount = 2).run()

    } finally {
      Await.result(system.terminate(), 3.second)
    }
  }
}
