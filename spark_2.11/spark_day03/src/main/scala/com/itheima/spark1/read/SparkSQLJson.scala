package com.itheima.spark1.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSQLJson {

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

    /// TODO: 方式一: 直接使用json方法读取  读取的是压缩文件 MR程序读取文件会按照文件名取判断是不是压缩类型是压缩类型就解压
    val jsonDF: DataFrame = spark.read.json("datas/json/2015-03-01-11.json.gz")

      //获取需要的字段   $是一个函数   将$"字段" 转化为列
      /**
        * implicit class StringToColumn(val sc: StringContext) {
        * def $(args: Any*): ColumnName = {
        * new ColumnName(sc.s(args: _*))
        * }
        * }
        */
      .select($"id", $"type", $"public", $"created_at")
    jsonDF.printSchema()
    jsonDF.show(10, false)

    println("=======================================================================")

    /// TODO: 方式二: 使用testFile读取 数据类型是DataSet 只有一个字段  value  value的数据是json数据
    // 从json格式数据中读取json数据  数据类型Dataset[String]
    val jsonDF2: Dataset[String] = spark.read.textFile("datas/json/2015-03-01-11.json.gz")

    //todo 使select 方法从json格式数据获取需要的字段   参数名:类型*  *表示可变参数
    //def select(cols: Column*): DataFrame   参数:可变参数Column  里面参数get_json_object的返回值是Column
    val jsonDF3: DataFrame = jsonDF2.select(
      //隐式转换   import org.apache.spark.sql.functions._ 的方法
      //def get_json_object(e: Column, path: String): Column
      //第一个参数:表示从哪个字段获取数据 使用$ 函数 将字符串转化为Column对象
      //第二个参数:提取出的字段名字          从json数据中提取字段相当于从对象中获取数据 使用$.字段名获取
      //.as("id") 表示起别名
      get_json_object($"value", "$.id").as("id"),
      //注意加逗号 不加不能循继续添加
      get_json_object($"value", "$.type").as("type"),
      get_json_object($"value", "$.public").as("public"),
      get_json_object($"value", "$.created_at").as("created_at")
    )
    jsonDF3.printSchema()
    jsonDF3.show(10, false)
    sc.stop()
  }
}
