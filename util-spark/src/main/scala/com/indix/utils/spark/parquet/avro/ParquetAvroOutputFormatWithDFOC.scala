package com.indix.utils.spark.parquet.avro

import com.indix.utils.spark.parquet.DirectParquetOutputCommitter
import org.apache.parquet.avro.AvroParquetOutputFormat

class ParquetAvroOutputFormatWithDFOC extends AvroParquetOutputFormat {

  import org.apache.hadoop.mapreduce
  import org.apache.hadoop.mapreduce.OutputCommitter
  import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat

  override def getOutputCommitter(context: mapreduce.TaskAttemptContext): OutputCommitter = {
    val output = FileOutputFormat.getOutputPath(context)
    new DirectParquetOutputCommitter(output, context)
  }
}
