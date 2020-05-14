package com.indix.utils.spark.parquet

import java.io.File

import com.google.common.io.Files
import com.indix.utils.spark.parquet.avro.ParquetAvroDataSource
import org.apache.commons.io.FileUtils
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.spark.sql.SparkSession
import org.scalactic.Equality
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper, equal}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import java.util.{Arrays => JArrays}

case class SampleAvroRecord(a: Int, b: String, c: Seq[String], d: Boolean, e: Double, f: collection.Map[String, String], g: Array[Byte])

class ParquetAvroDataSourceSpec extends FlatSpec with BeforeAndAfterAll with ParquetAvroDataSource {
  private var spark: SparkSession = _
  implicit val sampleAvroRecordEq = new Equality[SampleAvroRecord] {
    override def areEqual(left: SampleAvroRecord, b: Any): Boolean = b match {
      case right: SampleAvroRecord =>
        left.a == right.a &&
          left.b == right.b &&
          Equality.default[Seq[String]].areEqual(left.c, right.c) &&
          left.d == right.d &&
          left.e == right.e &&
          Equality.default[collection.Map[String, String]].areEqual(left.f, right.f) &&
          JArrays.equals(left.g, right.g)
      case _ => false
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession.builder().master("local[2]").appName("ParquetAvroDataSource").getOrCreate()
  }

  override protected def afterAll(): Unit = {
    try {
      spark.sparkContext.stop()
    } finally {
      super.afterAll()
    }
  }

  "AvroBasedParquetDataSource" should "read/write avro records as ParquetData" in {

    val outputLocation = Files.createTempDir().getAbsolutePath + "/output"

    val sampleRecords: Seq[SampleAvroRecord] = Seq(
      SampleAvroRecord(1, "1", List("a1"), true, 1.0d, Map("a1" -> "b1"), "1".getBytes),
      SampleAvroRecord(2, "2", List("a2"), false, 2.0d, Map("a2" -> "b2"), "2".getBytes),
      SampleAvroRecord(3, "3", List("a3"), true, 3.0d, Map("a3" -> "b3"), "3".getBytes),
      SampleAvroRecord(4, "4", List("a4"), true, 4.0d, Map("a4" -> "b4"), "4".getBytes),
      SampleAvroRecord(5, "5", List("a5"), false, 5.0d, Map("a5" -> "b5"), "5".getBytes)
    )

    val sampleDf = spark.createDataFrame(sampleRecords)

    sampleDf.rdd.saveAvroInParquet(outputLocation, sampleDf.schema, CompressionCodecName.GZIP)

    val sparkVal = spark

    import sparkVal.implicits._

    val records: Array[SampleAvroRecord] = spark.read.parquet(outputLocation).as[SampleAvroRecord].collect()

    records.length should be(5)
    // We use === to use the custom Equality defined above for comparing Array[Byte]
    // Ref - https://github.com/scalatest/scalatest/issues/491
    records.sortBy(_.a) === sampleRecords.sortBy(_.a)

    FileUtils.deleteDirectory(new File(outputLocation))
  }

}