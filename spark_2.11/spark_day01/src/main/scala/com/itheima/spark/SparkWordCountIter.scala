package com.itheima.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//todo spark Application
object SparkWordCountIter {

  def main(args: Array[String]): Unit = {

    //todo 1.构建sparkContext实例
    val config = new SparkConf()
      .setAppName(this.getClass.getSimpleName)
      .setMaster("local[2]")

    val sc: SparkContext = new SparkContext(config)

    //todo 2.读取文件 默认hdfs一个块 对应一个分区 在读取文件的时候可以设置分区
    //def textFile(
    //      path: String,
    //      minPartitions: Int = defaultMinPartitions): RDD[String] = withScope
    val inputRDD: RDD[String] = sc.textFile("/datas/sparkInput/wc.txt", 2)

    //todo 3.处理数据
    val wordcountRDD: RDD[String] = inputRDD.flatMap(_.split(" "))

    //todo  def mapPartitions[U: ClassTag](
    //      f: Iterator[T] => Iterator[U],   参数是一个函数:函数类型是 输入一个迭代器 输出一个迭代器
    //      preservesPartitioning: Boolean = false): RDD[U] = withScope
    val mapRDD: RDD[(String, Int)] = wordcountRDD.mapPartitions(iter => iter.map((_, 1)))

    //todo def reduceByKey(func: (V, V) => V): RDD[(K, V)]  参数是一个函数:函数类型输入两个 输出一个
    val wordsCountRDD: RDD[(String, Int)] = mapRDD.reduceByKey(_ + _).sortBy(_._2, false)

    //todo  def foreachPartition(f: Iterator[T] => Unit): Unit
    wordsCountRDD.foreachPartition(iter => iter.foreach { case (k, v) => println(s"$k->$v") })

    // TODO: 5、应用结束，关闭资源
    if(!sc.isStopped) sc.stop()
  }
}
