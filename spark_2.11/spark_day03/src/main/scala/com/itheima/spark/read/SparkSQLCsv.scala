package com.itheima.spark.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}

/// TODO: 读取csv数据
object SparkSQLCsv {

  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkSession和SparkContext对象
    val (spark, sc) = {

      //1.构建SparkSession实例
      val session: SparkSession = SparkSession.builder()
        .appName(this.getClass.getSimpleName.stripSuffix("$"))
        .master("local[3]")
        .getOrCreate()

      //2.构建SparkContext
      val context: SparkContext = session.sparkContext

      //3. 设置日志级别
      context.setLogLevel("WARN")
      // 4.返回值
      (session, context)
    }


    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._

    /// TODO: 读取的CSV数据 首行是列名称
    val csvDF: DataFrame = spark.read
      //字段分隔符：sep，默认值为逗号
      .option("sep", "\\t")
      //数据文件首行是否是列名称：header，默认为false
      .option("header", "true")
      //是否依据数据的值自动推断Schema信息：inferSchema，默认为false
      .option("inferSchema", "true")
      .csv("datas/ml-100k/u.dat")
    csvDF.printSchema()
    csvDF.show(10)


    /**
      * root
      * |-- userId: integer (nullable = true)
      * |-- movieId: integer (nullable = true)
      * |-- rating: integer (nullable = true)
      * |-- timestamp: integer (nullable = true)
      */
    println("========================================")
    /// TODO: 读取的csv数据首行不是列名称 自定义schema
    //def apply(fields: Seq[StructField]): StructType
    val structType: StructType = StructType(
      Array(
        //case class StructField(
        //    name: String,
        //    dataType: DataType,
        //    nullable: Boolean = true,
        //    metadata: Metadata = Metadata.empty)
        StructField("userId", IntegerType, true),
        StructField("movieId", IntegerType, true),
        StructField("rating", IntegerType, true),
        StructField("timestamp", IntegerType, true)
      ))

    val csv: DataFrame = spark.read
      .schema(structType)
      .option("sep", "\\t")
      .option("header", "false")
      .option("inferSchema", "false")
      .csv("datas/ml-100k/u.data")
    csv.printSchema()
    csv.show(10)

    sc.stop()
  }
}
