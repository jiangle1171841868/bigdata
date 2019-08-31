package com.itheima.spark1.read

import java.util.Properties

import org.apache.spark.{SparkContext, TaskContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLJdbc {

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
    import org.apache.spark.sql.functions._

    val url: String = "jdbc:mysql://node03:3306/test"
    val table: String = "so"
    //
    val props: Properties = new Properties()
    props.put("user", "root")
    props.put("password", "123456")
    props.put("driver", "com.mysql.jdbc.Driver")

    /// TODO: 方式一: 单分区读取数据  会产生数据倾斜
    val jdbcDF: DataFrame = spark.read
      //def jdbc(
      // url: String,
      // table: String,
      // properties: Properties 添加配置信息设置user 和password
      // ): DataFrame
      .jdbc(url, table, props)
    jdbcDF.printSchema()
    jdbcDF.show(10)
    //打印每个分区有多少条数据
    jdbcDF.foreachPartition { iter => println(s"p-${TaskContext.getPartitionId()}: ${iter.size}") }

    println("=======================================================================")

    /// TODO: 方式二:根据字段设置分区   按照字段  条件(下限值  上限值)  除以 分区数  平局分区  容易产生数据倾斜
    val jdbcDF2: DataFrame = spark.read
      //def jdbc(
      //      url: String,
      //      table: String,

      //      columnName: String,  分区字段
      //      lowerBound: Long,    下限值
      //      upperBound: Long,    上限值
      //      numPartitions: Int,  分区数

      //      connectionProperties: Properties): DataFrame
      .jdbc(
      url, table,
      "order_amt", 0L, 100L, 10,
      props
    )

    jdbcDF2.printSchema()
    jdbcDF2.show(10)
    //打印每个分区有多少条数据
    jdbcDF2.foreachPartition { iter => println(s"p-${TaskContext.getPartitionId()}: ${iter.size}") }
    /// TODO: 方式三:高度自由分区 自己设置分区条件

    val predicates: Array[String] = Array(
      //自定义第一个分区
      "order_amt<=6",
      "order_amt>=6 AND order_amt<10",
      "order_amt>=10 AND order_amt<15",

      "order_amt>=15 AND order_amt<20",
      "order_amt>=20 AND order_amt<30",
      "order_amt>=30"
    )
    val jdbcDF3 = spark.read
      // def jdbc(
      //      url: String,
      //      table: String,
      //      predicates: Array[String],  todo 条件的集合 一个数组
      //      connectionProperties: Properties): DataFrame
      .jdbc(
      url, table, predicates, props
    )

    jdbcDF3.createOrReplaceTempView("so")

    /// TODO: 对读取的mysql数据进行分析
    spark.sql(
      """
        |select
        | user_id , sum(order_amt) as sum_amt
        |from
        |  so
        |group by
        | user_id
        |order by
        |  sum_amt
        |limit
        | 10
      """.stripMargin).show(10, true)


    jdbcDF3.printSchema()
    jdbcDF3.show(10)
    //打印每个分区有多少条数据
    jdbcDF3.foreachPartition { iter => println(s"p-${TaskContext.getPartitionId()}: ${iter.size}") }

    Thread.sleep(1000000)
    sc.stop()
  }
}
