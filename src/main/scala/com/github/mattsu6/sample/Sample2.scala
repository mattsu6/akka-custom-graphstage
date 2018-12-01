package com.github.mattsu6.sample

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, GraphDSL, RunnableGraph, Sink, Source }
import akka.stream.{ ActorMaterializer, ClosedShape }

object Sample2 extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val out = Sink.seq[Int]

  val g = RunnableGraph.fromGraph(GraphDSL.create(out) { implicit builder => o =>
    import GraphDSL.Implicits._
    val in = Source(1 to 5)
    val flow = Flow[Int].map(_ * 10)

    in ~> flow ~> o

    ClosedShape
  })

  g.run().foreach(println)
}
