package com.itheima.spark.dataframe

import io.netty.util.internal.chmv8.ConcurrentHashMapV8.DoubleByDoubleToDouble
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}


case class MLRating(userId: Int, itemId: Double, rating: String, timestamp: Long)

object RDDToDataFrame {

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

    //隐式转换  发现调用方法不对时 就要考虑包的问题 是否有隐式转换
    import spark.implicits._

    //todo 2.读取数据 没有添加hdfs两个配置文件 就会从项目中读取 加了默认读取的是hdfs  需要在路径前面加.
    val inputRDD: RDD[String] = sc.textFile("datas/ml-100k/u.data", 4)

    val ratingsRDD: RDD[MLRating] = inputRDD.mapPartitions { iter =>
      iter.map { line =>
        //按照指标分割
        val Array(userId, itemId, rating, timestamp) = line.trim.split("\\t")
        //返回四元组  定义一个样例类 接收数据
        MLRating(userId.toInt, itemId.toDouble, rating.toString, timestamp.toLong)
      }
    }

    //todo 将RDD转化为DataFrame  隐式转换 需要导包  发现调用方法不对时 就要考虑导包

    val ratingsDF: DataFrame = ratingsRDD.toDF()
    ratingsDF.printSchema()
    ratingsDF.show(10, truncate = false)

    val ratingsDS: Dataset[MLRating] = ratingsRDD.toDS()
    ratingsDS.printSchema()
    ratingsDS.show(10, truncate = false)


    //todo 1.转换RDD的类型是ROW  数据使用ROW封装
    val rowRatingsRDD: RDD[Row] = inputRDD.mapPartitions { iter =>
      iter.map { line =>
        val Array(userId, itemId, rating, timestamp) = line.trim.split("\\t")
        Row(userId.toInt, itemId.toInt, rating.toDouble, timestamp.toLong)
      }
    }

    //todo 2.自定义Schema，结构化类型StructType，每列封装在StructField
    // def apply(fields: Seq[StructField]): StructType   参数是集合 集合里面是StructField
    //case class StructField(
    //    name: String,
    //    dataType: DataType,
    //    nullable: Boolean = true,
    //    metadata: Metadata = Metadata.empty)
    val ratingSchema: StructType = StructType(
      Array(
        StructField("userId", IntegerType, true),
        StructField("itemId", IntegerType, true),
        StructField("rating", DoubleType, true),
        StructField("timestamp", LongType, true)
      )
    )

    /// TODO: 调用函数转换
    //def createDataFrame(rdd: RDD[_], beanClass: Class[_]): DataFrame
    val frame: DataFrame = spark.createDataFrame(rowRatingsRDD, ratingSchema)
    frame.printSchema()
    frame.show(10)

    //转化为DataSet
    val set: Dataset[MLRating] = frame.as[MLRating]
    set.printSchema()
    set.show(10)


    sc.stop()
  }
}
