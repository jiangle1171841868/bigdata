package com.itheima.spark1.read

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLParquet {

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

    /// TODO: 方式一: 直接load读取
    //todo   读取文件 sparksql默认的数据类型是parquet  所以直接load读取  就可以读取parquet格式的数据
    val parquetDF: DataFrame = spark.read.load("datas/resources/users.parquet")
    parquetDF.printSchema()
    //参数:读取几条 truncate = false表示是否截取超过20的字段  默认长度是20
    parquetDF.show(10, truncate = false)


    /**
      * root
      * |-- name: string (nullable = true)
      * |-- favorite_color: string (nullable = true)
      * |-- favorite_numbers: array (nullable = true)  todo 注意:数据类型里面有array类型
      * |-- element: integer (containsNull = true)
      *
      * +------+--------------+----------------+
      * |name  |favorite_color|favorite_numbers|
      * +------+--------------+----------------+
      * |Alyssa|null          |[3, 9, 15, 20]  |
      * |Ben   |red           |[]              |
      * +------+--------------+----------------+
      */
    println("=======================================================================")

    /// TODO: 方式二:parquet 方法读取
    val parquetDF2: DataFrame = spark.read.parquet("datas/resources/users.parquet")
    parquetDF.printSchema()
    parquetDF.show(10, truncate = false)

    sc.stop()
  }
}
