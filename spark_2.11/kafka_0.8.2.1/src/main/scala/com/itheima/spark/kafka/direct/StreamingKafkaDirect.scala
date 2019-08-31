package com.itheima.spark.kafka.direct

import kafka.serializer.{Decoder, StringDecoder}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.reflect.ClassTag

object StreamingKafkaDirect {

  def main(args: Array[String]): Unit = {

    //大小写转化ctr+shift+u
    val CHECK_POINT_PATH: String = "/datas/spark/checkpoint"

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
      CHECK_POINT_PATH,
      () => ({
        val conf: SparkConf = new SparkConf()
          .setMaster("local[3]")
          .setAppName(this.getClass.getName.stripSuffix("$"))
          //设置每秒每分区读取的最大数据量(条数)
          .set("spark.streaming.kafka.maxRatePerPartition", "10000")

        val context = new StreamingContext(conf, Seconds(5))
        context.sparkContext.setLogLevel("WARN")
        context
      })
    )


    /// TODO: 设置检查点
    ssc.checkpoint(CHECK_POINT_PATH)

    /// TODO: 2. 使用户kafkaUtils old  simple读取kafka数据

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

    /// TODO: 3 处理数据
    /**
      * a.能对RDD处理就不对DStream处理 不能对RDD处理在对DStream处理
      * b.处理之前必先过滤(清楚数据结构)  从kafka中读取的数据是 k v结构(需要的数据在value里面)
      */
    val wordCountsDStream: DStream[(String, Int)] = kafkaDStream.transform { rdd =>
      //val xx: RDD[(String, String)] = rdd  RDD中的数据是元组(一定要了解数据结构 再操作)
      rdd
        .filter(line => line != null && line._2.trim.split("\\s+").length > 0)
        .mapPartitions { datas =>
          datas.flatMap(data => data._2.trim.split("\\s+").filter(word => word.length > 0))
        }
        .map(word => (word, 1))
    }
      .reduceByKey((v1, v2) => v1 + v2)

    /// TODO: 4 输出数据
    /**
      * a.先判断rdd是否为空
      * b.考虑降低分区数
      */

    wordCountsDStream.foreachRDD { (rdd, time) =>

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

    /// TODO: 5 启动streaming
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }
}
