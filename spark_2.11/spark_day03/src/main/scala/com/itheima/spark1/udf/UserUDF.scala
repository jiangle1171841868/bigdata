package com.itheima.spark1.udf

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction

object UserUDF {

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
        //todo 告知应用集成hive 读取元数据
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


    /// TODO: 自定义udf函数 将大写字母转换为小写字母  sql
    spark.udf.register(
      "low", //udf函数的名字
      (name: String) => name.toLowerCase //转化的函数 可以是匿名 也可以是引用函数
    )


    /// TODO: 执行sql语句  读取hive表数据 使用函数
    spark.sql(
      """
        |select
        |  empno,ename,low(ename),salary
        |from
        |  myhive.tb_emp_normal
      """.stripMargin).show(10, true)


    /// TODO: 在DSL中使用
    val low_name: UserDefinedFunction = udf((name: String) => name.toLowerCase)
    spark
      .read
      .table("myhive.tb_emp_normal")
      .select($"empno", $"ename", low_name($"ename"))
      .show(10)

    sc.stop()
  }
}
