package com.github.mattsu6.sample.distribution

case class Elements(value: Seq[Int]) extends Distributable[Elements] {

  override def distribute(size: Int): Seq[Elements] = {
    val n = if (size > value.size) {
      value.size
    } else {
      size
    }
    val (quot, rem) = (value.size / n, value.size % n)
    val (smaller, bigger) = value.splitAt(value.size - rem * (quot + 1))
    val cutted = smaller.grouped(quot) ++ bigger.grouped(quot + 1)
    if (size > n) {
      cutted.map(copy).toSeq ++ Seq.tabulate(size - n)(_ => Elements.empty)
    } else {
      cutted.map(copy).toSeq
    }
  }

  def +(that: Elements): Elements = copy(value ++ that.value)
}

object Elements {
  def empty: Elements = new Elements(Seq.empty)
}
