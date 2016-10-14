package com.indix.utils.spark.pail

import com.backtype.hadoop.pail._
import com.backtype.support.{Utils => PailUtils}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.{InputFormat, JobConf}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

trait PailDataSource {

  implicit class PailBasedReader(sc: SparkContext) {
    def pailFile[R: ClassTag](inputLocation: String): RDD[R] = {
      pailFileWithInfo[R](inputLocation).map(_._2)
    }

    def pailFileWithInfo[R: ClassTag](inputLocation: String) = {
      val pail = new Pail(inputLocation)
      val pailSpec = pail.getSpec
      val inputFormat = pail.getFormat.getInputFormatClass.asSubclass(classOf[InputFormat[PailRecordInfo, BytesWritable]])
      sc.hadoopFile(inputLocation, inputFormat, classOf[PailRecordInfo], classOf[BytesWritable])
        .map {
          case (recordInfo, recordInBytes) =>
            recordInfo -> pailSpec.getStructure.deserialize(recordInBytes.getBytes).asInstanceOf[R]
        }
    }
  }

  implicit class PailBasedWriter[R: ClassTag](rdd: RDD[R]) {
    def saveAsPail(outputLocation: String, pailSpec: PailSpec) = {
      val jobConf = new JobConf(rdd.context.hadoopConfiguration)

      PailUtils.setObject(jobConf, PailOutputFormat.SPEC_ARG, pailSpec)

      rdd.map { record =>
        val pailStruct = pailSpec.getStructure.asInstanceOf[PailStructure[R]]

        val attr = PailUtils.join(pailStruct.getTarget(record), "/")
        val recordInBytes = pailStruct.serialize(record)
        new Text(attr) -> new BytesWritable(recordInBytes)
      }.saveAsHadoopFile(outputLocation, classOf[Text], classOf[BytesWritable], classOf[PailOutputFormat], jobConf)
    }
  }

}
