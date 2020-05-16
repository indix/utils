package com.indix.gocd.utils.utils

case class Tuple2[Left, Right](internal: (Left, Right)) {
  def _1: Left = internal._1
  def _2: Right = internal._2
}
