package com.itheima.spark.streaming

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}

object SpackStreamingWordCount {

  def main(args: Array[String]): Unit = {

    val ssc: StreamingContext = {
      val conf: SparkConf = new SparkConf()
        .setMaster("local[3]")
        .setAppName(this.getClass.getName.stripSuffix("$"))
      //创建   def this(conf: SparkConf, batchDuration: Duration) 戳进Duration 源码可以看到 设置时间有Milliseconds Seconds Minutes
      val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))
      ssc.sparkContext.setLogLevel("WARN")
      ssc
    }


    /// TODO: 2 .读取流式数据
    // def socketStream[T: ClassTag](
    //      hostname: String,  todo 主机名
    //      port: Int,        todo  端口
    //      converter: (InputStream) => Iterator[T],
    //      storageLevel: StorageLevel  todo   receivers划分的block的存储级别
    //    ): ReceiverInputDStream[T]
    val inputDS: ReceiverInputDStream[String] = ssc.socketTextStream("node03", 9999, StorageLevel.MEMORY_ONLY)

    //直接对DStream处理
    val wordCountsDStream: DStream[(String, Int)] = inputDS
      //读取数据先过滤
      .filter(line => line != null && line.trim.split("\\s+").length > 0)
      .flatMap(line => line.trim.split("\\s+").filter(word => word.length > 0))
      .map(word => (word, 1))
      .reduceByKey((v1, v2) => v1 + v2)

    //输出函数 将数据输出到控制台  默认打印10条 底层调用的是foreachRDD  输出函数只有foreachRDD
    wordCountsDStream.print()

    //对RDD处理
    val wcDStream: DStream[(String, Int)] = inputDS.transform { rdd =>
      rdd
        .filter(line => line != null && line.trim.split("\\s+").length > 0)
        .flatMap(line => line.trim.split("\\s+").filter(word => word.length > 0))
        .mapPartitions(datas => datas.map(word => (word, 1)))
        .reduceByKey((v1, v2) => v1 + v2)
    }

    //todo 使用输出函数输出
    //def foreachRDD(foreachFunc: (RDD[T], Time) => Unit): Unit
        wcDStream.foreachRDD { (rdd, time) =>

      //时间是毫秒格式  需要转化成字符串格式  SimpleDateFormat线程不安全 大数据不用 使用线程安全的FastDateFormat
      val stime: String = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(time.milliseconds)

      //打印时间
      println("-------------------------------------------")
      println(s"Time: $stime")
      println("-------------------------------------------")

      //todo 注意:输出的时候就要考虑rdd是否为空
      if (!rdd.isEmpty()) {
        rdd
          //输出就要考虑减少分区数
          .coalesce(1)
          .foreachPartition { datas => datas.foreach(data => println(data))
          }
      }
    }

    //针对流式处理 需要接收器 start方法 创建接收器 可以有多个(多个数据源)
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }

}
