package com.indix.gocd.utils.utils

import java.util

import scala.collection.JavaConversions._

object Maps {
  case class MapBuilder[K, V](internal: Map[K, V]) {

    def _with(k: K, v: V) = MapBuilder(internal ++ Map[K, V](k -> v))

    def remove(k: K): MapBuilder[K, V] = {
      val newInternal = internal.filterNot(tuple => k.equals(tuple._1))
      MapBuilder(newInternal)
    }

    def buildJavaMap(): util.Map[K, V] = mapAsJavaMap(internal)

    def build: Map[K, V] = internal
  }

  def builder[K, V] = MapBuilder(Map[K, V]())
}
