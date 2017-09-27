package com.indix.utils.spark.parquet

import com.indix.utils.spark.SparkJobSpec
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.functions._

class DirectParquetOutputCommitterSpec extends SparkJobSpec {
  override val appName = "DirectParquetOutputCommitterSpec"
  override val taskRetries = 2

  it should "not fail with file already exists on subsequent retries" in {
    spark.conf
      .set("spark.sql.parquet.output.committer.class", "com.indix.utils.spark.parquet.DirectParquetOutputCommitter")

    val exception = intercept[org.apache.spark.SparkException] {
      sqlContext
        .range(10)
        .toDF()
        .withColumn("dummy", udf((x: Int) => x / (x - 1)).apply(col("id")))
        .write
        .parquet("/tmp/parquet/dummy")
    }

    val path = new Path("/tmp/parquet/dummy")
    val fs = path.getFileSystem(new Configuration())
    fs.delete(path)

  }


}
