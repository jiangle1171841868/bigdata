package com.itheima.spark.streaming

import java.util.Date

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SpackStreamingOutputHdfs {

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

    //对RDD处理
    val wcDStream: DStream[(String, Int)] = inputDS.transform { rdd =>
      rdd
        .filter(line => line != null && line.trim.split("\\s+").length > 0)
        .flatMap(line => line.trim.split("\\s+").filter(word => word.length > 0))
        .mapPartitions(datas => datas.map(word => (word, 1)))
        .reduceByKey((v1, v2) => v1 + v2)
    }

    //todo 将处理后的数据 保存到hdfs中  要求保存的时间格式 是yyyy-MM-dd/log- HH:mm:ss类型的文件
    //参数 time是处理批数据的时间戳

    //wcDStream.saveAsTextFiles("aaa", "bbb")

    wcDStream.foreachRDD { (rdd, time) =>

      val stime: String = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(new Date(time.milliseconds))

      println("-------------------------------------------")
      println(s"Time: $stime")
      println("-------------------------------------------")
      //rdd判断是否为空 不为就输出数据
      if (!rdd.isEmpty()) {
        //输出文件就要考虑降低分区数
        rdd
          .coalesce(1)
          //按照文件格式输出  截取字符串从索引0开始  包括头部包括尾
          .saveAsTextFile(s"/datas/streaming/hdfs-sink/${stime.substring(0, 10)}/logs-$time")

      }
    }

    //start方法启动receiver
    ssc.start()
    ssc.awaitTermination()
    //优雅的停止 就是任务完成之后再停止
    //def stop(stopSparkContext: Boolean, stopGracefully: Boolean): Unit
    ssc.stop(stopSparkContext = true, stopGracefully = true)

  }

}
