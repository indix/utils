package com.indix.utils.store

import java.io.{ByteArrayOutputStream, File, IOException}
import java.nio.file.{FileVisitOption, Files, Paths}
import java.util.UUID

import com.twitter.chill
import com.twitter.chill.{Input, Output, ScalaKryoInstantiator}
import org.apache.commons.io.FileUtils
import org.rocksdb._
class RocksMap(name: String = "test",
               rocksDBRoot: String = "/tmp/rocksdb",
               writeBufferSize: Long = 1024 * 1024 * 20) {

  RocksDB.loadLibrary()
  val options = new Options()
    .setCreateIfMissing(true)
    .setWriteBufferSize(writeBufferSize)
    .setInfoLogLevel(InfoLogLevel.ERROR_LEVEL)

  val instantiator = new ScalaKryoInstantiator
  instantiator.setRegistrationRequired(true)
  val kryo = instantiator.newKryo()


  def getRandomString: String = UUID.randomUUID().toString match {
    case randomString if new File(rocksDBRoot + randomString + name).exists() => getRandomString
    case randomString if !new File(rocksDBRoot + randomString + name).exists() => randomString
  }

  val pathString = rocksDBRoot + getRandomString + name

  var db = RocksDB.open(options, pathString)

  val writeOptions = (new WriteOptions).setDisableWAL(true).setSync(false)

  def put[X, Y](key: X, value: Y) = {
    db.put(writeOptions, serialize(key), serialize(value))
  }

  def serialize[X](key: X): Array[Byte] = {
    val output: Output = new chill.Output(new ByteArrayOutputStream)
    kryo.writeClassAndObject(output, key)
    output.toBytes
  }

  def get[X, Y](key: X): Option[Y] = {
    val valueBytes: Array[Byte] = db.get(serialize(key))
    if (valueBytes == null) {
      None
    } else {
      Some(deserialize[Y](valueBytes))
    }
  }

  def deserialize[T](data: Array[Byte]): T = {
    kryo.readClassAndObject(new Input(data)).asInstanceOf[T]
  }

  def remove[X](key: X): Unit = {
    db.remove(serialize(key))
  }

  def keysIterator[X]() = {
    val it = db.newIterator()
    it.seekToFirst()
    new Iterator[X] {
      def hasNext = it.isValid

      def next = {
        val next = deserialize[X](it.key())
        it.next()
        next
      }
    }
  }

  def valuesIterator[Y]() = {
    val it = db.newIterator()
    it.seekToFirst()
    new Iterator[Y] {
      def hasNext = it.isValid

      def next = {
        val next = deserialize[Y](it.value())
        it.next()
        next
      }
    }
  }

  def toIterator[X, Y]() = {
    val it = db.newIterator()
    it.seekToFirst()
    new Iterator[(X, Y)] {
      def hasNext = it.isValid

      def next = {
        val next = (deserialize[X](it.key()), deserialize[Y](it.value()))
        it.next()
        next
      }
    }
  }

  def compact() = {
    db.compactRange()
  }

  def clear() = {
    val it = db.newIterator()
    it.seekToFirst()
    while (it.isValid) {
      db.remove(it.key())
      it.next()
    }
    db.flush(new FlushOptions)
  }

  def drop() = {
    try {
      FileUtils.deleteDirectory(new java.io.File(pathString))
    } catch {
      case e: IOException => {
        e.printStackTrace()
        throw e
      }
    }
  }

  def close() = {
    if (db != null) {
      db.close()
    }
    options.close()
  }
}