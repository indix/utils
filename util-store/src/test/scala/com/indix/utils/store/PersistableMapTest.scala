package com.indix.utils.store

import org.scalatest._

class PersistableMapTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {
  before {
    val path = new java.io.File("/tmp/rocks")
    if(!path.exists()){
      path.mkdirs()
    }
  }

  "PersistableMap" should "spill to disk" in {
    val persistableMap = new PersistableMap[String, Int]("test", threshold = 3)
    persistableMap.put("hello", 1)
    persistableMap.get("hello") should be(Some(1))
    persistableMap.isSpilled should be(false)
    persistableMap.put("world", 2)
    persistableMap.get("world") should be(Some(2))
    persistableMap.isSpilled should be(false)
    persistableMap._size should be(2)
    persistableMap.put("hello", 3)
    persistableMap.get("hello") should be(Some(3))
    persistableMap.isSpilled should be(false)
    persistableMap._size should be(2)
    persistableMap.put("yoyo", 4)
    persistableMap.get("yoyo") should be(Some(4))
    persistableMap.isSpilled should be(true)
    persistableMap._size should be(3)
    persistableMap.close()
  }
}