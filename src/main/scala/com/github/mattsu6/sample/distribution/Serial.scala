package com.github.mattsu6.sample.distribution

import akka.stream.{ Attributes, Inlet, Outlet, UniformFanInShape }
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }

/** Fan-inグラフDSL. 複数の入力を１つにまとめ，出力する.
  *
  * '''Emits''' 全ての入力ポートが利用可能な時
  * '''Backpressures''' 出力ポートがバックプレッシャーを実行した時
  * '''Completes''' アップストリームが完了した時
  * '''Cancels''' ダウンストリームがキャンセルした時
  *
  * @param inputPorts 入力ポートの数.
  * @param f 入力値を１つにまとめる関数
  * @tparam T まとめる型
  */
class Serial[T](inputPorts: Int)(f: (T, T) => T) extends GraphStage[UniformFanInShape[T, T]] {

  val ins: Seq[Inlet[T]] = for (i <- 0 until inputPorts) yield Inlet[T](s"Serial.in$i")
  val out: Outlet[T] = Outlet[T]("Serial.out")

  override def shape: UniformFanInShape[T, T] = UniformFanInShape(out, ins: _*)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    var pending = 0

    override def preStart(): Unit = ins.foreach(pull)

    ins.foreach { in =>
      setHandler(
        in,
        new InHandler {
          override def onPush(): Unit = {
            pending -= 1
            if (pending == 0) pushAll()
          }
        }
      )
    }

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pending += ins.size
        if (pending == 0) pushAll()
      }
    })

    private def pushAll(): Unit = {
      push(out, ins.map(grab).reduce(f))
      ins.foreach(pull)
    }
  }
}

object Serial {

  def apply[T](inputPorts: Int)(f: (T, T) => T): Serial[T] = new Serial(inputPorts)(f)
}
