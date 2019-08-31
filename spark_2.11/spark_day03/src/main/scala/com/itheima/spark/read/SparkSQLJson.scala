package com.itheima.spark.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}


/// TODO: 读取json类型数据
object SparkSQLJson {
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

    import spark.implicits._

    /// TODO: 使用sparkSql读取json数据  json是压缩文件
    val jsonDF: DataFrame = spark.read
      .json("datas/json/2015-03-01-11.json.gz")
      //选择需要的字段 会爆红需要 隐式转换  导包
      .select($"id", $"type", $"public", $"created_at")

    jsonDF.printSchema()
    jsonDF.show(10)


    println("========================================")

    // TODO: 从Spark 2.x中提供对JSON格式数据处理的数据
    val jsonDS: Dataset[String] = spark.read.textFile("datas/json/2015-03-01-11.json.gz")
    jsonDF.printSchema()
    jsonDF.show(10)

    println("========================================")

    // 从JSON格式字段中提取数据  $"id", $"type", $"public", $"created_at"
    import org.apache.spark.sql.functions._
    //  def select(cols: Column*): DataFrame
    jsonDS.select(
      //def get_json_object(e: Column, path: String): Column
      //todo value字段中的数据是json数据  解析json数据
      //todo 第一个字段是   $"字段名"  第二个是需要解析的字段名   获取json数据就是 json名.字段名  as是起别名的意思
      get_json_object($"value", "$.id").as("id"),
      get_json_object($"value", "$.type").as("type"),
      get_json_object($"value", "$.public").as("public"),
      get_json_object($"value", "created_at").as("created_at")
    ).show(10)


    sc.stop()
  }

}
