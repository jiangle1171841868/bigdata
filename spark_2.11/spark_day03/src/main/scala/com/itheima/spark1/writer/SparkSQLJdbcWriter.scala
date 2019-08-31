package com.itheima.spark1.writer

import java.util.Properties

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.{SparkContext, TaskContext}

/// TODO: 将数据写到mysql数据库中
object SparkSQLJdbcWriter {

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

    val url: String = "jdbc:mysql://node03:3306/test"
    val table: String = "userinfo"
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

    /// TODO: 将数据写到mysql数据库
    //def jdbc(url: String, table: String, connectionProperties: Properties)
    jdbcDF.write.mode(SaveMode.Append).jdbc(url, table, props)


    Thread.sleep(1000000)
    sc.stop()
  }
}
