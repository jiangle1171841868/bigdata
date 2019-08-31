package com.itheima.spark.read

import java.util.Properties

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

/// TODO: 从mysql中读取数据
object SparkSQLMySQL {

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

    /// TODO: 2、从MySQL表中读取销售订单表的数据db_orders.so
    val url = "jdbc:mysql://node03:3306/db_orders"
    val table: String = "db_orders.so"
    val props: Properties = new Properties()
    props.put("user", "root")
    props.put("password", "123456")
    props.put("driver", "com.mysql.jdbc.Driver")

    /// TODO: 方式一 单分区
    //def jdbc(url: String, table: String, properties: Properties): DataFrame
    val mysql01: DataFrame = spark.read.jdbc(url, table, props)
    mysql01.printSchema()
    mysql01.show(10)


    println("========================================")

    /// TODO: 方式二  并发读取
    //def jdbc(
    //      url: String,
    //      table: String,
    //      columnName: String,
    //      lowerBound: Long,
    //      upperBound: Long,
    //      numPartitions: Int,
    //      connectionProperties: Properties): DataFrame
    spark.read.jdbc(
      url,
      table,
      "order_amt",
      10L,
      200L,
      5,
      props
    )


    sc.stop()
  }
}
