package com.itheima.spark.kafka.receiver


import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingKafkaReceiver {

  def main(args: Array[String]): Unit = {

    //大小写转化ctr+shift+u
    val CHECKPOINTPATH: String = "/datas/spark/checkpoint"

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
      CHECKPOINTPATH,
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

    /// TODO: 2. 使用户kafkaUtils old  hight读取kafka数据
    /**
      * def createStream(
      * ssc: StreamingContext,
      * zkQuorum: String,            //Zookeeper quorum (hostname:port,hostname:port,..)
      * groupId: String,             //The group id for this consumer
      * topics: Map[String, Int],    //Map of (topic_name to numPartitions) to consum
      * storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
      * ): ReceiverInputDStream[(String, String)]
      */
    val zkQuorum: String = "node01:2181,node02:2181,node03:2181"
    val groupId: String = "test"
    //topic以及分区数  可以多个
    val topics: Map[String, Int] = Map("testTopic" -> 3)
    val kafkaDStream: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(
      ssc,
      zkQuorum,
      groupId,
      topics,
      StorageLevel.MEMORY_AND_DISK
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
