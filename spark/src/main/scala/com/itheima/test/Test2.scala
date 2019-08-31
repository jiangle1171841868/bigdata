package com.itheima.test

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object Test2 {
  def main(args: Array[String]): Unit = {
    //todo 1.构建spark程序的入口 saprkContext对象

    val sparkConf: SparkConf = new SparkConf().setAppName("wordCountSort").setMaster("local[2]")
    val sc: SparkContext = new SparkContext(sparkConf)

    //todo 2.读取数据
    val inputRDD: RDD[String] = sc.textFile("/datas/sparkInput/wc.txt")

    //todo 3.调用函数处理数据  使用groupByKey() 进行分组 迭代器里面是value
    val map: RDD[(String, Iterable[Int])] = inputRDD.flatMap(_.trim.split(" ")).map((_, 1)).groupByKey()
    map.foreach(println)
  }

}
