package com.itheima.spark.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object Example {

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


  sc.stop()
  }
}
