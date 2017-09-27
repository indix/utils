package com.indix.utils.spark.pail

import java.util

import com.backtype.hadoop.pail.{PailFormatFactory, PailSpec, PailStructure}
import com.backtype.support.{Utils => PailUtils}
import com.google.common.io.Files
import com.indix.utils.spark.SparkJobSpec
import org.apache.commons.io.FileUtils
import org.scalatest.Matchers._

import scala.collection.JavaConverters._
import scala.util.Random

case class User(name: String, age: Int)

class UserPailStructure extends PailStructure[User] {
  override def isValidTarget(dirs: String*): Boolean = true

  override def getType: Class[_] = classOf[User]

  override def serialize(user: User): Array[Byte] = PailUtils.serialize(user)

  override def getTarget(user: User): util.List[String] = List(user.age % 10).map(_.toString).asJava

  override def deserialize(serialized: Array[Byte]): User = PailUtils.deserialize(serialized).asInstanceOf[User]
}

class PailDataSourceSpec extends SparkJobSpec with PailDataSource {
  override val appName: String = "PailDataSource"

  val userPailSpec = new PailSpec(PailFormatFactory.SEQUENCE_FILE, new UserPailStructure)

  "PailBasedReaderWriter" should "read/write user records from/into pail" in {
    val output = Files.createTempDir()
    val users = (1 to 100).map { index => User(s"foo$index", Random.nextInt(40))}
    spark.sparkContext.parallelize(users)
      .saveAsPail(output.getAbsolutePath, userPailSpec)

    val input = output.getAbsolutePath
    val total = spark.sparkContext.pailFile[User](input)
      .map(u => u.name)
      .count()

    total should be(100)
    FileUtils.deleteDirectory(output)
  }
}