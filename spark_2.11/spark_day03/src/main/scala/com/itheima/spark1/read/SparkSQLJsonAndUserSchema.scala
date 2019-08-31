package com.itheima.spark1.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.types.{BooleanType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSQLJsonAndUserSchema {

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
    /// TODO: json数据可以自动推断数据类型  自定义数据类型  为成功

    val schema = StructType(

      StructField("id", IntegerType, true) ::
        StructField("type", StringType, true) ::
        StructField("public", BooleanType, true) ::
        StructField("created_at", StringType, true) :: Nil
    )
    val jsonDF: DataFrame = spark.read

      .schema(schema)
      .json("datas/json/2015-03-01-11.json.gz")


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


    Thread.sleep(100000)
    sc.stop()
  }
}
