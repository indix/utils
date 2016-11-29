package com.indix.utils.store

import scala.collection.mutable

class PersistableMap[A, B](name: String, threshold: Int) extends scala.collection.mutable.Map[A,B] {
  private val inMemoryMap = mutable.Map[A, B]()

  private lazy val onDiskMap = new RocksMap(name)

  private var spilled = false

  var _size = 0

  override def size = _size

  def isSpilled = spilled

  def spillIfThresholdCrossed() = {
    if (_size == threshold) {
      inMemoryMap.map { case (key, value) => onDiskMap.put[A, B](key, value) }
      inMemoryMap.clear()
      spilled = true
    }
  }

  override def put(key: A, value: B) = {
    val previousValue = get(key)
    previousValue match {
      case None => _size = _size + 1
      case Some(value) =>
    }
    if (spilled) {
      onDiskMap.put[A, B](key, value)
    } else {
      inMemoryMap.put(key, value)
      spillIfThresholdCrossed()
    }
    previousValue
  }

  override def get(key: A): Option[B] = {
    if (spilled) {
      onDiskMap.get[A, B](key)
    } else {
      inMemoryMap.get(key)
    }
  }

  def getOrElse(key: A, elseValue: B) = {
    get(key) match {
      case Some(value) => value
      case None => elseValue
    }
  }

  def compact() = {
    if (isSpilled) {
      onDiskMap.compact()
    }
  }

  override def clear() = {
    if (isSpilled) {
      onDiskMap.clear()
    } else {
      inMemoryMap.clear()
    }
  }

  def close() = {
    if (isSpilled) {
      onDiskMap.close()
      onDiskMap.drop()
    } else {
      inMemoryMap.clear()
    }
  }

  override def remove(key: A) = {
    val deletedValue: Option[B] = get(key)
    deletedValue match {
      case Some(value) => _size = _size - 1
      case None =>
    }
    if (isSpilled) {
      onDiskMap.remove[A](key)
    } else {
      inMemoryMap.remove(key)
    }

    deletedValue
  }

  def keysIterator(implicit ordering: Ordering[A]): Iterator[A] = {
    if (isSpilled) {
      onDiskMap.keysIterator[A]()
    } else {
      inMemoryMap.keys.toList.sorted.toIterator
    }
  }

  override def valuesIterator: Iterator[B] = {
    if (isSpilled) {
      onDiskMap.valuesIterator[B]()
    } else {
      inMemoryMap.valuesIterator
    }
  }

  def sortedIterator(implicit ordering: Ordering[A]): Iterator[(A, B)] = {
    if (isSpilled) {
      onDiskMap.toIterator[A, B]()
    } else {
      inMemoryMap.toList.sortBy(_._1).toIterator
    }
  }


  def +=(kv: (A, B)): PersistableMap.this.type = {
    this.put(kv._1,kv._2)
    this
  }

  def -=(key: A): PersistableMap.this.type = {
    this.remove(key)
    this
  }

  override def iterator: Iterator[(A, B)] = {
    if (isSpilled) {
      onDiskMap.toIterator[A, B]()
    } else {
      inMemoryMap.toList.toIterator
    }
  }
}