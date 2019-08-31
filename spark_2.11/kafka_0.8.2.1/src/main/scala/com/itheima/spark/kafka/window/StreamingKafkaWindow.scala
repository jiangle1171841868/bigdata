package com.itheima.spark.kafka.window

import kafka.serializer.StringDecoder
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream, MapWithStateDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}

/// TODO: 需求 对每批次的数据(2s)   每隔4秒统计前6秒的数据  只会统计前4秒的数据 不会累加更前的数据
/// TODO: 两个变量 窗口间隔(就是统计前几秒的时间)  滑动间隔(就是间隔几秒统计一次)
object StreamingKafkaWindow {

  //大小写转化ctr+shift+u
  val CHECK_POINT_PATH: String = "/datas/spark/checkpoint/00002"

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


    /// TODO: 2 数据ETL处理  处理一:获取需要的数据
    /**
      * a.能对RDD处理就不对DStream处理 不能对RDD处理在对DStream处理
      * b.处理之前必先过滤(清楚数据结构)
      * c.处理数据之后 返回值 要考虑转换数据类型  因为操作之后默认String
      * todo  注意: 对数据类型修改之后可能会爆红 因为DStream的返回值类型已经确定 修改就行了
      *
      * 从kafka中读取的数据是 k v结构(需要的数据在value里面)
      *
      * 分析数据结构:
      * k:
      * v:
      * orderId,provinceId,orderPrice
      * 201710261645320002,12,20.00
      *
      * 需要获取,provinceId和provinceId
      * 处理后:将provinceId作为key 按照省份实时统计订单销售额
      */


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

    val stateDStream: DStream[(Int, Long)] = windowDStream
      //a.能对RDD处理就不对DStream处理 不能对RDD处理在对DStream处理
      .transform { rdd =>
      rdd
        //b.处理之前必先过滤(清楚数据结构)
        .filter(line => line != null && line._2.trim.split(",").length >= 3)
        .mapPartitions { datas =>
          datas.map { data =>
            val Array(orderId, provinceId, orderPrice) = data._2.trim.split(",")
            //c.处理数据之后 返回值 要考虑转换数据类型  因为操作之后默认String
            //统计各个省份的订单量 一条数据 就是一条订单
            (provinceId.toInt, 1L)
          }
        }
        //聚合操作 减少实时统计的输入量
        .reduceByKey(_ + _)
    }


    /// TODO: 3 输出数据
    /**
      * a.先判断rdd是否为空
      * b.考虑降低分区数
      */

    stateDStream.foreachRDD { (rdd, time) =>

      val batchTime: String = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(time.milliseconds)

      println("-------------------------------------------")
      println(s"Time: $batchTime")
      println("-------------------------------------------")

      //a.先判断rdd是否为空
      if (!rdd.isEmpty()) {
        rdd
          //b.考虑降低分区数
          .coalesce(1)
          .foreachPartition { datas =>
            datas.foreach(println)
          }
      }
    }

  }

}
