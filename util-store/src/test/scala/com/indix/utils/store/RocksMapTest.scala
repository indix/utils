package com.indix.utils.store

import java.io.Serializable
import java.nio.file.{Paths, Files}

import org.apache.commons.io.FileUtils
import org.scalatest.{Matchers, FlatSpec}


case class TestObject(a: Int, b: String, c: Array[Int], d: Array[String]) extends Serializable {

  def equals(other: TestObject): Boolean = {
    this.a.equals(other.a) && this.b.equals(other.b) && this.c.sameElements(other.c) && this.d.sameElements(other.d)
  }

}

case class ComplexTestObject(a: Int, b: TestObject) extends Serializable {
  def equals(other: ComplexTestObject): Boolean = {
    this.a.equals(other.a) && this.b.equals(other.b)
  }
}

class RocksMapTest extends FlatSpec with Matchers {

  "RocksMap" should "serialize and deserialize the keys and values" in {
    val db = new RocksMap("test")

    val a: Int = 1
    val b: String = "hello"
    val c: Array[Int] = Array(1, 2, 3)

    val d: Array[String] = Array("a", "b", "c")

    val serialized_a = db.serialize(a)
    val serialized_b = db.serialize(b)
    val serialized_c = db.serialize(c)
    val serialized_d = db.serialize(d)
    val serialized_TestObject = db.serialize(TestObject(a, b, c, d))
    val serialized_ComplexObject = db.serialize(ComplexTestObject(a, TestObject(a, b, c, d)))

    db.deserialize[Int](serialized_a) should be(a)
    db.deserialize[String](serialized_b) should be(b)
    db.deserialize[Array[Int]](serialized_c) should be(c)
    db.deserialize[Array[String]](serialized_d) should be(d)
    db.deserialize[TestObject](serialized_TestObject).equals(TestObject(a, b, c, d)) should be(true)
    db.deserialize[ComplexTestObject](serialized_ComplexObject).equals(ComplexTestObject(a, TestObject(a, b, c, d))) should be(true)
    db.drop()
    db.close()
  }

  it should "put and get values" in {
    val db = new RocksMap("test")

    db.put(1, 1.0)
    db.get[Int, Double](1).getOrElse(0) should be(1.0)
    db.clear()
    db.drop()
    db.close()
  }

  it should "remove values" in {
    val db = new RocksMap("test")

    db.put(1, 1L)
    db.get[Int, Long](1).getOrElse(0) should be(1L)
    db.remove(1)
    db.get[Int, Long](1) should be(None)
    db.drop()
    db.close()
  }

  it should "clear all the values" in {
    val db = new RocksMap(name = "test")
    db.put(1, "hello")
    db.put(2, "yello")
    db.get(1) should not be (None)
    db.get(2) should not be (None)
    db.clear()
    db.get(1) should be(None)
    db.get(2) should be(None)
    db.drop()
    db.close()
  }

  it should "clear the data files when drop is called" in {
    val db = new RocksMap(name = "test")
    Files.exists(Paths.get(db.pathString)) should be (true)
    db.drop()
    Files.exists(Paths.get(db.pathString)) should be (false)
    db.close()
  }


}
