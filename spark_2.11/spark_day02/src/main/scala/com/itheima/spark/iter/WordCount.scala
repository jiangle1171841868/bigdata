package com.itheima.spark.iter

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {

  def main(args: Array[String]): Unit = {

    //todo 1.构建spark程序的入口SparkContext上下文实例对象
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("SparkWordCount")
      .setMaster("local[1]") //以本地模式运行

    //参数:config: SparkConf  需要SparkConf对象
    val sc: SparkContext = new SparkContext(sparkConf)


    val inputRDD: RDD[String] = sc.textFile("./datas/group/group.data")

    val wordCountRDD: RDD[(String, String)] = inputRDD.mapPartitions { iter =>
      iter.map(line => (line.split("\\s+")(0), line.split("\\s+")(1))) }

    wordCountRDD.foreachPartition { iter =>
      iter.foreach { case (k, v) => println(s"$k->$v") }
    }
  }
}