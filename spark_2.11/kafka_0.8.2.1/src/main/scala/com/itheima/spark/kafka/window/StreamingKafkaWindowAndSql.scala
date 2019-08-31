package com.itheima.spark.kafka.window

import kafka.serializer.StringDecoder
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/// TODO: 需求 对每批次的数据(2s)   每隔4秒统计前6秒的数据  只会统计前4秒的数据 不会累加更前的数据
/// TODO: 两个变量 窗口间隔(就是统计前几秒的时间)  滑动间隔(就是间隔几秒统计一次)  使用sql分析 数据
object StreamingKafkaWindowAndSql {

  //大小写转化ctr+shift+u
  val CHECK_POINT_PATH: String = "datas/spark/checkpoint/00006"

  //定义时间间隔
  val STREAMING_BATCH_INTERVAL = 2
  val STREAMING_WINDOW_INTERVAL = STREAMING_BATCH_INTERVAL * 3
  val STREAMING_WINDOW_SLIDE_INTERVAL = STREAMING_BATCH_INTERVAL * 2


  def main(args: Array[String]): Unit = {


    /// TODO: 1. 创建streamingContext对象
    /**
      * def getActiveOrCreate(
      * checkpointPath: String, //检查点hdfs路径
      * creatingFunc: () => StreamingContext,
      * hadoopConf: Configuration = SparkHadoopUtil.get.conf,
      * createOnError: Boolean = false
      * ): StreamingContext
      */
    val ssc: StreamingContext = StreamingContext.getActiveOrCreate(

      //todo 当流式应用第一次运行的时候(检查点目录不存在 会创建StreamingContext实例对象) 创建检查点目录 检查点存在的时候 会获取之前的状态
      CHECK_POINT_PATH,
      () => ({
        val conf: SparkConf = new SparkConf()
          .setMaster("local[3]")
          .setAppName(this.getClass.getName.stripSuffix("$"))
          //设置每秒每分区读取的最大数据量(条数)
          .set("spark.streaming.kafka.maxRatePerPartition", "10000")

        val context = new StreamingContext(conf, Seconds(STREAMING_BATCH_INTERVAL))

        //设置检查点目录  通常存储在hdfs上
        /**
          * 应用停止之后
          *       - 检查点会记录消费数据的偏移量 不会漏掉数据
          *       - 数据的状态也会被保存
          */
        context.checkpoint(CHECK_POINT_PATH)

        /// TODO: 2 调用函数处理数据  调用的时候要在创建StreamingContext实例的里面
        processData(context)
        context
      })
    )

    ssc.sparkContext.setLogLevel("WARN")


    /// TODO: 3 启动streaming
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }

  /// TODO: 抽取函数 传递ssc梳理对象处理数据

  def processData(ssc: StreamingContext) = {
    /// TODO: 1. 使用户kafkaUtils old  simple读取kafka数据

    /**
      * def createDirectStream[
      * K: ClassTag,  //topic中key的数据类型
      * V: ClassTag,  //topic中value的数据类型
      * KD <: Decoder[K]: ClassTag,  //表示反序列化  从文件中读取序列换文件 需要解码
      * VD <: Decoder[V]: ClassTag] (
      * ssc: StreamingContext,
      * kafkaParams: Map[String, String],
      * topics: Set[String]
      * ): InputDStream[(K, V)]
      */

    /// TODO: 具体怎么设置参数 看源码 里面都有介绍
    val kafkaParams: Map[String, String] = Map(
      /**
        * equires "metadata.broker.list" or "bootstrap.servers"
        * *   to be set with Kafka broker(s) (NOT zookeeper servers), specified in
        * *   host1:port1,host2:port2 form.
        */
      "bootstrap.servers" -> "node01:9092,node02:9092,node03:9092",

      /**
        * If not starting from a checkpoint, "auto.offset.reset" may be set to "largest" or "smallest"
        */
      "auto.offset.reset" -> "largest"
    )
    //topic使用set集合封装 可以多个 可以去重
    //Names of the topics to consumese
    val topics: Set[String] = Set("testTopic")

    val kafkaDStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaParams,
      topics
    )


    /// TODO: 窗口函数每隔4秒统计前6秒的数据
    /**
      * def window(windowDuration: Duration, slideDuration: Duration): DStream[T]
      * windowDuration: Duration
      *   - 参数一:窗口时间间隔  必须是batchIntevar的整数倍 must be a multiple of this DStream's batching interval
      * slideDuration: Duration
      *   - 参数二:滑动时间间隔  必须是batchIntevar的整数倍
      *
      */
    val windowDStream: DStream[(String, String)] = kafkaDStream.window(Seconds(STREAMING_WINDOW_INTERVAL), Seconds(STREAMING_WINDOW_SLIDE_INTERVAL))

    /// TODO: 3 输出数据
    /**
      * a.先判断rdd是否为空
      * b.考虑降低分区数
      */

    windowDStream.foreachRDD { (rdd, time) =>

      val batchTime: String = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(time.milliseconds)

      println("-------------------------------------------")
      println(s"Time: $batchTime")
      println("-------------------------------------------")

      /// TODO: 需求:将窗口函数输出的结果适应sql分析
      /**
        * 分析:
        *   - 1.将RDD转换为DataFrame  RDD+schema
        *       - 转化方式两种
        *          - a.RDD[caseclass]  反射转换 ToDF方法
        *          - b.RDD[Row] 自定义一个schema 使用sparkSession实例对象创建DF
        *   - 2.创建sparkSession实例对象
        *   - 3.使用DSL函数分析
        **/

      //RDD[ROW]
      if (!rdd.isEmpty()) {

        //对rdd进行处理 将RDD缓存到内存中
        rdd.persist(StorageLevel.MEMORY_AND_DISK)

        // 1.RDD[Row]
        val rowRDD: RDD[Row] = rdd
          .filter(line => line != null && line._2.trim.split(",").length >= 3)
          .mapPartitions { datas =>
            //val xx: Iterator[(String, String)] =datas
            datas.map { data =>
              val Array(orderId, provinceId, orderPrice) = data._2.trim.split(",")
              // 返回
              Row(orderId, provinceId.toInt, orderPrice.toDouble)
            }
          }

        //2.自定义schema
        val schema = StructType(
          StructField("orderId", StringType, true) ::
            StructField("provinceId", IntegerType, true) ::
            StructField("orderPrice", DoubleType, true) ::
            Nil
        )


        //3.构建sparkSession实例
        val spark: SparkSession = SparkSession.builder()
          //构建sparkSession实例需要配置信息 通过rdd.sparkContext.getConf获取 已经有conf实例对象不能再构建(单例)
          .config(rdd.sparkContext.getConf)
          //join agg集合之后 会产生shuffle more分区数是200 所以哟啊设置
          .config("spark.sql.shuffle.partitions", "3")
          .getOrCreate()


        //4.将RDD转化为DF
        //导包
        import spark.implicits._
        val dataDF: DataFrame = spark.createDataFrame(rowRDD, schema)

        //5.使用DSL分析
        dataDF
          .select($"provinceId")
          .groupBy($"provinceId")
          .count()
          .show(10, true)

        //6.分析结束 释放缓存的RDD
        rdd.unpersist()
      }
    }


  }
}
