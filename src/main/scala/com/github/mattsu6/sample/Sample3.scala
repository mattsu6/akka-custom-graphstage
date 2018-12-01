package com.github.mattsu6.sample

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source }
import akka.stream.{ ActorMaterializer, ClosedShape }

object Sample3 extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val out = Sink.seq[Int]

  val g = RunnableGraph.fromGraph(GraphDSL.create(out) { implicit builder => o =>
    import GraphDSL.Implicits._
    val in = Source(1 to 5)

    // 奇数のみ通過
    val oddFilter = Flow[Int].filter(_ % 2 != 0)
    // 偶数のみ通過
    val evenFilter = Flow[Int].filter(_ % 2 == 0)

    val to100x = Flow[Int].map(_ * 100)
    val to10x = Flow[Int].map(_ * 10)

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    in ~> bcast ~> oddFilter  ~> to10x ~> merge ~> o
          bcast ~> evenFilter ~> to100x  ~> merge

    ClosedShape
  })

  g.run().foreach(println)
}
