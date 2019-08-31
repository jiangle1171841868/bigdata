package com.itheima.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object WordCountLinux {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println("Usage: SparkWordCountSubmit <input> <output> .........")
      System.exit(1)
    }

    //todo 1.构建spark程序的入口SparkContext上下文实例对象
    //todo 对Spark Application进行配置设置，设置应用的名称，设置应用运行的地方（--master）
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("SparkWordCount")
      .setMaster("local[2]") //以本地模式运行

    //参数:config: SparkConf  需要SparkConf对象
    val sc: SparkContext = new SparkContext(sparkConf)

    //todo 2.读取文件 封装成RDD集合
    // textFile 一行一行的读  封装成RDD
    val inputRDD: RDD[String] = sc.textFile(args(0))
    //inputRDD.foreach(println)
    //todo 3.调用RDD集合的函数处理数据
    //1.切割 压平
    val wordcountsRDD: RDD[(String, Int)] = inputRDD.flatMap(_.split(" "))
      //2.每个单词计数为1  将每个单词转化为对偶元组(单词,1)
      .map((_, 1))
      //先按照key进行分组 在对value统计聚合
      .reduceByKey(_ + _)
    wordcountsRDD.foreach(println)

    ///todo 4.输出结果RDD到本地系统  目录不能存在
    wordcountsRDD.saveAsTextFile(args(1) + "_" + System.currentTimeMillis())

    // TODO: 5、应用结束，关闭资源
    if (!sc.isStopped) sc.stop()
  }

}
