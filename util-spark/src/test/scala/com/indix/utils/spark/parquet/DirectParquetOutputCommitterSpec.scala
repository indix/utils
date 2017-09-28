package com.indix.utils.spark.parquet

import java.io.File

import com.indix.utils.spark.SparkJobSpec
import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.spark.SparkException
import org.apache.spark.sql.SaveMode
import org.scalatest.Matchers

class TestDirectParquetOutputCommitter(outputPath: Path, context: TaskAttemptContext)
  extends DirectParquetOutputCommitter(outputPath, context) {

  override def commitTask(taskContext: TaskAttemptContext): Unit = {
    if (taskContext.getTaskAttemptID.getId == 0)
      throw new SparkException("Failing first attempt of task")
    else
      super.commitTask(taskContext)
  }

}

class DirectParquetOutputCommitterSpec extends SparkJobSpec with Matchers {
  override val appName = "DirectParquetOutputCommitterSpec"
  override val taskRetries = 2
  override val sparkConf = Map(("spark.sql.parquet.output.committer.class", "com.indix.utils.spark.parquet.TestDirectParquetOutputCommitter"))
  var file: File = _

  override def beforeAll() = {
    super.beforeAll()
    file = File.createTempFile("parquet", "")
  }

  override def afterAll() = {
    super.afterAll()
    FileUtils.deleteDirectory(file)
  }

  it should "not fail with file already exists on subsequent retries" in {
    lazy val result = sqlContext
      .range(10)
      .toDF()
      .write
      .mode(SaveMode.Overwrite)
      .parquet(file.toString)

    //Subsequent retry of task will not throw an exception
    noException should be thrownBy result
  }

}
