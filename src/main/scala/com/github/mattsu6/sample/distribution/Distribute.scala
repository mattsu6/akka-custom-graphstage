package com.github.mattsu6.sample.distribution

import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.stream.{ Attributes, Inlet, Outlet, UniformFanOutShape }

/** Fan-outグラフDSL. １つの入力値を分散させ複数の出力ポートに出力する.
  * [[akka.stream.scaladsl.Broadcast]]と似ているが, Broadcastとは異なり出力を`distribute`によって分散させる
  *
  * '''Emits''' 入力ポートが利用可能な時
  * '''Backpressures''' 出力ポートのどれかがバックプレッシャーを実行した時
  * '''Completes''' アップストリームが完了した時
  * '''Cancels''' どれか１つでもダウンストリームがキャンセルした時
  *
  * @param number 分散させる数, 1以上を指定する
  * @tparam T 分散させる型
  */
class Distribute[T <: Distributable[T]](size: Int) extends GraphStage[UniformFanOutShape[T, T]] {

  require(size >= 1, "Must one or more output ports")

  val in: Inlet[T] = Inlet[T]("Distribute.in")
  val outs: Seq[Outlet[T]] = Seq.tabulate(size)(i => Outlet[T](s"Distribute.out$i"))

  override val shape: UniformFanOutShape[T, T] = UniformFanOutShape(in, outs: _*)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private var pendingCount = size

    setHandler(
      in,
      new InHandler {
        override def onPush(): Unit = {
          pendingCount = size
          val elements = grab(in).distribute(size)

          if (elements.size != outs.size) {
            outs.foreach { o =>
              fail(
                o,
                new UnsupportedOperationException(
                  s"Incorrect return size at `distribute` method. required: ${outs.size} actual: ${elements.size}"))
            }
          }

          outs.zip(elements).foreach { e =>
            if (!isClosed(e._1)) {
              push(e._1, e._2)
            }
          }
        }
      }
    )

    outs.zipWithIndex.foreach { e =>
      setHandler(e._1, new OutHandler {
        override def onPull(): Unit = {
          pendingCount -= 1
          tryPull()
        }
      })
    }
    private def tryPull(): Unit =
      if (pendingCount == 0 && !hasBeenPulled(in)) pull(in)
  }

  override def toString = "Distribute"
}

object Distribute {

  def apply[T <: Distributable[T]](size: Int): Distribute[T] = new Distribute[T](size)
}
