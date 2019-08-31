package com.itheima.spark1.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

object SparkSQLCsv {

  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkSession和SparkContext对象
    val (spark, sc) = {

      //1.构建SparkSession实例
      val session: SparkSession = SparkSession.builder() //构造模式创建实例
        .appName(this.getClass.getSimpleName.stripSuffix("$"))
        .master("local[3]")
        //spark-default配置文件里面里面的kv属性
        //.config("","")
        //单例对象   底层还是sparkContext 进行了封装
        .getOrCreate()

      //2.构建SparkContext
      val context: SparkContext = session.sparkContext

      //3. 设置日志级别
      context.setLogLevel("WARN")
      // 4.返回值
      (session, context)
    }

    //todo 导包
    import spark.implicits._
    //import org.apache.spark.sql.functions._

    /// TODO: 读取文件 需要设置三个参数:
    //  todo 第一个sep  分隔符(csv默认分割符是逗号 可以自定义csv的分隔符)
    //  todo 注意: 读取csv文件 可以指定分隔符 但是分割只能单一字符串
    //  todo 第二个参数:header 头部是否是字段名称 默认是false
    //  todo 第三个参数:inferSchema 自动推断数据类型 默认是false

    /// TODO: 方式一:读取的csv文件 头部不是字段名称  需要自定义schema
    //case class StructType(fields: Array[StructField])
    val schema: StructType = StructType(
      Array(
        //case class StructField(
        //    name: String,
        //    dataType: DataType,
        //    nullable: Boolean = true,   是否可以为空 true 表示可以为空
        //    metadata: Metadata = Metadata.empty)
        StructField("userId", StringType, true),
        StructField("movieId", StringType, true),
        StructField("rating", IntegerType, true),
        StructField("timestamp", LongType, true)
      )
    )
    val csvDF: DataFrame = spark.read
      //csv文件 没有头部字段    设置自定义的schema
      .schema(schema)
      //设置三个参数 kv的方式设置
      //1.sep 分隔符 \\t
      .option("sep", "\\t")
      .option("header", "false")
      .option("inferSchema", "false")
      .csv("datas/ml-100k/u.data")

    csvDF.printSchema()
    csvDF.show(10, false)
    println("=======================================================================")

    /// TODO: 方式二:有头部信息
    val csvDF2: DataFrame = spark.read
      .option("sep", "\\t")
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("datas/ml-100k/u.dat")
    csvDF2.printSchema()
    csvDF2.show(10, false)

    println("=======================================================================")

    /// TODO: 保存数据为json格式
    spark.read
      .option("sep", "\\t")
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("datas/ml-100k/u.dat")

      //减少分区数量
      .coalesce(1)
      //保存为json格式
      .write
      //设置保存模式
      /**
        * 5种保存模式
        * public enum SaveMode{
        * Append
        * Overwrite
        * ErrorIfExists
        * Ignore
        * }
        */
      .mode(SaveMode.Overwrite)
      .json("datas/ml-json/")

    sc.stop()
  }
}
