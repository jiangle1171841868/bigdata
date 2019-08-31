package com.itheima.spark.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLParquet {
  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkSession和SparkContext对象
    val (spark, sc) = {

      //a.构建SparkSession实例
      val session: SparkSession = SparkSession.builder()
        .appName(this.getClass.getSimpleName.stripSuffix("$"))
        .master("local[3]")
        .getOrCreate()

      //b.构建SparkContext
      val context: SparkContext = session.sparkContext

      //c. 设置日志级别
      context.setLogLevel("WARN")
      //d.返回值
      (session, context)
    }

    import spark.implicits._
    //todo 2.读取parquet格式数据 sparksql默认读取的方式
    val frame: DataFrame = spark.read.parquet("datas/resources/users.parquet")
    frame.printSchema()
    frame.show(10)
    println("========================================")

    val frame2: DataFrame = spark.read.load("datas/resources/users.parquet")
    frame2.printSchema()
    frame2.show(10)


    sc.stop()
  }
}
