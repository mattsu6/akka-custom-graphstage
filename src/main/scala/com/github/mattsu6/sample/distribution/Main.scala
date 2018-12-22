package com.github.mattsu6.sample.distribution

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, GraphDSL, Sink, Source }
import akka.stream.{ ActorMaterializer, FlowShape }

object Main extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val distributedFlow = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val distribute = b.add(Distribute[Elements](3))
    val serial = b.add(Serial[Elements](3)((a, b) => a + b))
    val flow = Flow[Elements].map { elems =>
      println(s"Thread id: ${Thread.currentThread().getId} for ${elems.value}"); Elements(elems.value.map(_ * 10))
    }

    distribute ~> flow.async ~> serial
    distribute ~> flow.async ~> serial
    distribute ~> flow.async ~> serial

    FlowShape(distribute.in, serial.out)
  })

  Source.single(Elements(Seq(1, 2, 3, 4))).via(distributedFlow).runWith(Sink.seq).foreach(println)

}
