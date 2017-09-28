package com.indix.utils.spark

import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

abstract class SparkJobSpec extends FlatSpec with BeforeAndAfterAll {
  val appName: String
  val taskRetries: Int = 1
  val sparkConf: Map[String, String] = Map()

  @transient var spark: SparkSession = _
  lazy val sqlContext = spark.sqlContext

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession.builder().master(s"local[2, $taskRetries]").appName(appName).getOrCreate()
    sparkConf.foreach {
      case (k, v) => spark.conf.set(k, v)
    }
  }
}
