package com.indix.gocd.utils.utils

trait Function[I, O] {
  def apply(input: I): O
}

abstract class VoidFunction[I] extends Function[I, Nothing]{
  def execute(i: I): Nothing
  override def apply(input: I): Nothing = execute(input)
}

abstract class Predicate[T] extends Function[T, Boolean] {
  def execute(i: T): Boolean
  override def apply(input: T): Boolean = execute(input)
}
