package com.itheima.spark1.hive

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object SparkSqlHIve {


  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkSession和SparkContext对象
    val (spark, sc) = {

      //1.构建SparkSession实例
      val session: SparkSession = SparkSession.builder() //构造模式创建实例
        .appName(this.getClass.getSimpleName.stripSuffix("$"))
        .master("local[3]")
        .config("spark.sql.shuffle.partitions", "3")
        //spark-default配置文件里面里面的kv属性
        //.config("","")
        //单例对象   底层还是sparkContext 进行了封装
        //todo 告知应用集成hive  读取元数据
        .enableHiveSupport()
        .getOrCreate()

      //2.构建SparkContext
      val context: SparkContext = session.sparkContext

      //3. 设置日志级别
      context.setLogLevel("WARN")
      // 4.返回值
      (session, context)
    }

    import org.apache.spark.sql.functions._
    import spark.implicits._
    // id  |   userid   | age  | gender  |  item_id   | behavior_type  | item_category  |    date     | province
    //需求:求每天每个省份订单的Top3
    spark
      .read
      .table("myhive.taobao2")
      .select($"date", $"province", $"behavior_type")
      .filter("behavior_type=4")
      .groupBy($"date", $"province")
      // df.groupBy("department").agg(max("age"), sum("expense"))
      // df.groupBy("department").agg(Map("age" -> "max","expense" -> "sum"))
      .agg(sum($"behavior_type"))
      .limit(10)
      .show(10)

    Thread.sleep(10000000)
    sc.stop()
  }
}
