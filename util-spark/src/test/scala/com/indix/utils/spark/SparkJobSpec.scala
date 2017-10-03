package com.indix.utils.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

abstract class SparkJobSpec extends FlatSpec with BeforeAndAfterEach with BeforeAndAfterAll {
  val appName: String
  val taskRetries: Int = 2
  val sparkConf: Map[String, String] = Map()

  @transient var spark: SparkSession = _
  lazy val sqlContext = spark.sqlContext

  override protected def beforeAll(): Unit = {
    spark = SparkSession.builder()
      .master(s"local[2, $taskRetries]").appName(appName)
      .getOrCreate()

    sparkConf.foreach {
      case (k, v) => spark.conf.set(k, v)
    }
  }

  override protected def afterAll() = {
    SparkSession.clearDefaultSession()
    SparkSession.clearActiveSession()
  }

}
