package com.github.mattsu6.sample

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }

object Sample1 extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Source(1 to 5).map(_ * 10).runWith(Sink.seq).foreach(println)
}
