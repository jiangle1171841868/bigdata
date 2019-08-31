package com.itheima.spark.kafka.state

import kafka.serializer.StringDecoder
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingKafkaTotal {

  def main(args: Array[String]): Unit = {

    //大小写转化ctr+shift+u
    val CHECK_POINT_PATH: String = "datas/spark/checkpoint/states"

    /// TODO: 1. 创建streamingContext对象
    val ssc = {
      val conf: SparkConf = new SparkConf()
        .setMaster("local[3]")
        .setAppName(this.getClass.getName.stripSuffix("$"))
        //设置每秒每分区读取的最大数据量(条数)
        .set("spark.streaming.kafka.maxRatePerPartition", "10000")

      val context = new StreamingContext(conf, Seconds(3))
      context.sparkContext.setLogLevel("WARN")
      context
    }

    //设置检查点
    ssc.checkpoint("datas/spark/checkpoint/states")

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


    /// TODO: 3 数据ETL处理  处理一:获取需要的数据
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
    val wordCountsDStream = kafkaDStream
      //a.能对RDD处理就不对DStream处理 不能对RDD处理在对DStream处理
      .transform { rdd =>
      rdd
        //b.处理之前必先过滤(清楚数据结构)
        .filter(line => line != null && line._2.trim.split(",").length >= 3)
        .mapPartitions { datas =>
          datas.map { data =>
            val Array(orderId, provinceId, orderPrice) = data._2.trim.split(",")
            //c.处理数据之后 返回值 要考虑转换数据类型  因为操作之后默认String
            (provinceId.toInt, orderPrice.toDouble)
          }
        }
        //聚合操作 减少实时统计的输入量
        .reduceByKey(_ + _)
    }

    /// TODO: 处理二: 根据省份id实时统计销售额  使用updateStateByKey
    // 此处不能对RDD进行处理 使用DStream处理数据
    /**
      * def updateStateByKey[S: ClassTag](
      * updateFunc: (Seq[V],Option[S]) => Option[S]
      * ): DStream[(K, S)]
      *
      * todo :分析
      * 一个参数:参数类型是函数
      * 函数类型:输入两个参数  输出一个
      * (Seq[V],Option[S]) => Option[S]
      *      - Seq[v] 是系列化的集合 里面保存的是这批次数据相同key value的集合
      *      - Option[S]  是当前批次key以前的状态 可能有 可能没有所以是Option类型
      * 返回值:
      *      - Option[S] 将之前批次key数据取出  和当前批次相加  将修改后的数值更新到状态
      * 泛型
      *      - 里面的泛型就是value的数据类型
      */
    val stateDStream: DStream[(Int, Double)] = wordCountsDStream.updateStateByKey(
      (values: Seq[Double], state: Option[Double]) => {

        //a.计算当前批次数据的销售额
        val value_total: Double = values.sum

        //b.获取之前的总销售额 没有就是设置默认值0.0
        val state_total: Double = state.getOrElse(0.0)

        //c.计算总销售额
        val total: Double = value_total + state_total

        //d.返回最新的销售额  Option类型 有数据就返回Some 没有数据就返回None
        Some(total)
      }
    )


    /// TODO: 4 输出数据
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

    /// TODO: 5 启动streaming
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }

}
