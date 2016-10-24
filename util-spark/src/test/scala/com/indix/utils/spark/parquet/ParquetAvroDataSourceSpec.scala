package com.indix.utils.spark.parquet

import java.io.File

import com.google.common.io.Files
import com.indix.utils.spark.parquet.avro.ParquetAvroDataSource
import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.Matchers.{be, convertToAnyShouldWrapper}
import org.apache.parquet.hadoop.metadata.CompressionCodecName

case class SampleAvroRecord(a: Int, b: String, c: Seq[String], d: Boolean, e: Double, f: collection.Map[String,String], g: Seq[Byte])

class ParquetAvroDataSourceSpec extends FlatSpec with BeforeAndAfterAll with ParquetAvroDataSource {
  private var spark: SparkSession = _

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
      SampleAvroRecord(1, "1", List("a1"), true, 1.0d, Map("a1" -> "b1"), Seq("1".toByte)),
      SampleAvroRecord(2, "2", List("a2"), false, 2.0d, Map("a2" -> "b2"), Seq("2".toByte)),
      SampleAvroRecord(3, "3", List("a3"), true, 3.0d, Map("a3" -> "b3"), Seq("3".toByte)),
      SampleAvroRecord(4, "4", List("a4"), true, 4.0d, Map("a4" -> "b4"), Seq("4".toByte)),
      SampleAvroRecord(5, "5", List("a5"), false, 5.0d, Map("a5" -> "b5"), Seq("5".toByte))
    )

    val sampleDf = spark.createDataFrame(sampleRecords)

    sampleDf.rdd.saveAvroInParquet(outputLocation, sampleDf.schema, CompressionCodecName.GZIP)

    val sparkVal = spark

    import sparkVal.implicits._

    val records: Array[SampleAvroRecord] = spark.read.parquet(outputLocation).as[SampleAvroRecord].collect()

    records.length should be(5)
    records.sortBy(_.a) should be (sampleRecords.sortBy(_.a))

    FileUtils.deleteDirectory(new File(outputLocation))
  }

}