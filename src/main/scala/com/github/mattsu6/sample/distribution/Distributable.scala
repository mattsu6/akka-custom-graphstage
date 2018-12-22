package com.github.mattsu6.sample.distribution

trait Distributable[A] {
  def distribute(size: Int): Seq[A]
}
