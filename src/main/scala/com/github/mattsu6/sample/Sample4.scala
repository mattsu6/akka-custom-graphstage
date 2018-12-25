package com.github.mattsu6.sample

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ GraphDSL, Merge, Sink, Source }
import akka.stream.{ ActorMaterializer, SourceShape }

/** GraphDSLを使ってSourceを作る例*/
object Sample4 extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val out = Sink.seq[Int]

  val source = Source.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val s1 = Source(1 to 5)
    val s2 = Source(6 to 10)

    val merge = builder.add(Merge[Int](2))

    s1 ~> merge
    s2 ~> merge

    SourceShape(merge.out)
  })

  source.map(_ * 10).runWith(Sink.seq).foreach(println)
}
