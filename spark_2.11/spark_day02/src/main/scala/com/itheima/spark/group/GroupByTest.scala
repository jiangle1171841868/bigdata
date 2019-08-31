package com.itheima.spark.group

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

object GroupByTest {
  def main(args: Array[String]): Unit = {
    //todo 1.构建SparkContext对象
    val sc: SparkContext = {
      val sparkConf: SparkConf = new SparkConf()
        //去掉stripSuffix  object类前面有$  使用方法去掉
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[3]")
      SparkContext.getOrCreate(sparkConf)
    }
    //设置日志级别
    sc.setLogLevel("WARN")

    //todo 1.读取数据
    val inputRDD: RDD[String] = sc.textFile("datas/group/group.data", 3)

    //todo 处理数据 设置数据类型之后 下面修改 导致RDD数据类型改变  就会全爆红
    //分析数据类型   aa 78  处理为需要的类型(aa,78)
    val dataRDD = inputRDD
      .filter(line => line != null && line.trim.split("\\s+").length == 2)
      .mapPartitions { iter => {
        iter.map(data => {
          val Array(word, count) = data.trim.split("\\s+")
          (word, count.toInt)
        })
      }
      }
      //分组
      //def groupByKey(): RDD[(K, Iterable[V])]
      .groupByKey()
      .mapPartitions { iter =>
        iter.map {
          case (word, counts) => {
            val values = counts.toList.sortBy(-_).take(3)
            (word, values)
          }
        }
      }

    dataRDD.foreachPartition { iter =>
      iter.foreach {
        case (word, counts) =>
          println(counts.map(count => (word, count)))
      }
    }
    sc.stop()

  }

}
