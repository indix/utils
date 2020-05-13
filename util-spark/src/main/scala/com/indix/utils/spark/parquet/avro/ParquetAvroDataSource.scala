package com.indix.utils.spark.parquet.avro

import java.nio.ByteBuffer
import java.sql.Timestamp
import java.util

import org.apache.spark.sql.avro.SchemaConverters
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.{GenericRecord, IndexedRecord}
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.bdgenomics.utils.misc.HadoopUtil
import org.apache.parquet.avro.{AvroParquetInputFormat, AvroParquetOutputFormat}
import org.apache.parquet.hadoop.ParquetOutputFormat
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.hadoop.util.ContextUtil

import scala.reflect.ClassTag


class AvroFormatter extends Serializable {
  /**
   * This function constructs converter function for a given sparkSQL datatype. This is used in
   * writing Avro records out to disk
   */
  def createConverterToAvro(
                             dataType: DataType,
                             structName: String,
                             recordNamespace: String): (Any) => Any = {
    dataType match {
      case BinaryType => (item: Any) =>
        item match {
          case null => null
          case bytes: Array[Byte] => ByteBuffer.wrap(bytes)
        }
      case ByteType | ShortType | IntegerType | LongType |
           FloatType | DoubleType | StringType | BooleanType => identity
      case _: DecimalType => (item: Any) => if (item == null) null else item.toString
      case TimestampType => (item: Any) =>
        if (item == null) null else item.asInstanceOf[Timestamp].getTime
      case ArrayType(elementType, _) =>
        val elementConverter = createConverterToAvro(elementType, structName, recordNamespace)
        (item: Any) => {
          if (item == null) {
            null
          } else {
            val sourceArray = item.asInstanceOf[Seq[Any]]
            val sourceArraySize = sourceArray.size
            val targetArray = new util.ArrayList[Any](sourceArraySize)
            var idx = 0
            while (idx < sourceArraySize) {
              targetArray.add(elementConverter(sourceArray(idx)))
              idx += 1
            }
            targetArray
          }
        }
      case MapType(StringType, valueType, _) =>
        val valueConverter = createConverterToAvro(valueType, structName, recordNamespace)
        (item: Any) => {
          if (item == null) {
            null
          } else {
            val javaMap = new util.HashMap[String, Any]()
            item.asInstanceOf[Map[String, Any]].foreach { case (key, value) =>
              javaMap.put(key, valueConverter(value))
            }
            javaMap
          }
        }
      case structType: StructType =>
        val schema: Schema = SchemaConverters.toAvroType(
          structType,nullable = false, structName, recordNamespace)
        val fieldConverters = structType.fields.map(field =>
          createConverterToAvro(field.dataType, field.name, recordNamespace))
        (item: Any) => {
          if (item == null) {
            null
          } else {
            val record = new Record(schema)
            val convertersIterator = fieldConverters.iterator
            val fieldNamesIterator = dataType.asInstanceOf[StructType].fieldNames.iterator
            val rowIterator = item.asInstanceOf[Row].toSeq.iterator

            while (convertersIterator.hasNext) {
              val converter = convertersIterator.next()
              record.put(fieldNamesIterator.next(), converter(rowIterator.next()))
            }
            record
          }
        }
    }
  }

}

trait ParquetAvroDataSource {

  implicit class AvroBasedParquetSource(sc: SparkContext) {
    def avroBasedParquet[R <: IndexedRecord](schema: Schema, inputLocation: String)(implicit ev1: Manifest[R]) = {
      val job = HadoopUtil.newJob(sc)
      AvroParquetInputFormat.setAvroReadSchema(job, schema)
      val avroType: Class[R] = manifest[R].runtimeClass.asInstanceOf[Class[R]]
      sc.newAPIHadoopFile(inputLocation, classOf[AvroParquetInputFormat[R]], classOf[Void], avroType, ContextUtil.getConfiguration(job))
        .map(_._2)
    }
  }

  implicit class AvroBasedParquetSinkRDD[R <: IndexedRecord : ClassTag](rdd: RDD[R]) {
    def saveAvroInParquet(outputLocation: String, schema: Schema, compression: CompressionCodecName = CompressionCodecName.UNCOMPRESSED, useDFOC: Boolean = true) = {
      val job = HadoopUtil.newJob(rdd.context.hadoopConfiguration)
      AvroParquetOutputFormat.setSchema(job, schema)
      ParquetOutputFormat.setCompression(job, compression)
      ParquetOutputFormat.setPageSize(job, 32 * 1024 * 1024) // 128 MB seems way too large
      val outputFormatter = if (useDFOC) classOf[ParquetAvroOutputFormatWithDFOC] else classOf[AvroParquetOutputFormat[GenericRecord]]
      rdd.map(r => null.asInstanceOf[Void] -> r)
        .saveAsNewAPIHadoopFile(outputLocation, classOf[Void], classOf[IndexedRecord], outputFormatter, ContextUtil.getConfiguration(job))
    }

  }

  implicit class AvroBasedParquetSinkDataFrame(rdd: RDD[Row]) {
    def saveAvroInParquet(outputLocation: String, sparkSchema: StructType, compression: CompressionCodecName = CompressionCodecName.UNCOMPRESSED, useDFOC: Boolean = true) = {
      val schema = toAvroSchema(sparkSchema)
      val job = HadoopUtil.newJob(rdd.context.hadoopConfiguration)
      AvroParquetOutputFormat.setSchema(job, schema)
      ParquetOutputFormat.setCompression(job, compression)
      ParquetOutputFormat.setPageSize(job, 32 * 1024 * 1024)
      val outputFormatter = if(useDFOC) classOf[ParquetAvroOutputFormatWithDFOC] else classOf[AvroParquetOutputFormat[GenericRecord]]
      rdd.map((r: Row) => null.asInstanceOf[Void] -> new AvroFormatter().createConverterToAvro(sparkSchema, "topLevelRecord", "")(r).asInstanceOf[GenericRecord])
        .saveAsNewAPIHadoopFile(outputLocation, classOf[Void], classOf[IndexedRecord], outputFormatter, ContextUtil.getConfiguration(job))
    }
  }

  def toAvroSchema(schema: StructType): Schema = {
    val recordNamespace = ""
    SchemaConverters.toAvroType(schema,nullable = false, "topLevelRecord", recordNamespace)
  }

}
