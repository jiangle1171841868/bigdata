package com.itheima.spark.demo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object LogAnalyze {

  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkContext对象
    val sc: SparkContext = {
      val sparkConf: SparkConf = new SparkConf()
        .setAppName("SparkWordCount")
        .setMaster("local[5]")
      SparkContext.getOrCreate(sparkConf)
    }
    //设置日志级别
    sc.setLogLevel("WARN")

    //todo 2.读取数据
    val inputRDD: RDD[String] = sc.textFile("datas/logs/access.log",2)

    //todo 3.处理数据  获取pv(url)第6个字段  uv(ip)第1个字段  ref第11个字段

    val logRDD: RDD[(String, String, String)] = inputRDD
      //1.数据过滤 数据不为空  字符串长度大于等于11(ref为第11个字段所以长度必须大于等于11才有效)  trim去掉左右空格
      .filter(line => line != null && line.trim.split("\\s+").length >= 11)
      //2.mapPartitions函数切割字符串  根据索引 获取需要的数据 组成三元组
      .mapPartitions { iter => {
      //对每一条数据进行处理
      iter.map { log =>
        val arr: Array[String] = log.trim.split("\\s+")
        //todo 返回三元组 (pv,uv,ref)
        (arr(5), arr(0), arr(10))
        //todo 过滤出ual不为空的数据
      }.filter { tuple => tuple._1 != null && tuple._1.length > 0 }
    }
    }

    //下面要对RDD数据进行 需要将RDD加载到内存中
    logRDD.cache()

    //加载到内存是懒函数 需要action函数触发
    println(logRDD.count())

    //todo pv分析   只要统计 就要考虑是否需要去重 一条数据就是一个url 不用去重 可以直接统计
    val pvcount: Long = logRDD.count()
    println("pv数:" + pvcount)

    //todo uv分析 一个ip可能有多条记录 需要去重  将ip取出转化为只含有ip的数组 进行去重再计数
    val uvCount: Long = logRDD.mapPartitions { iter => iter.map { tuple => tuple._2 } }
      .distinct()
      .count()

    println("uv数:" + uvCount)
    //todo ref分析 首先过滤判断ref是否为空 长度是否大于0  将数据转化为二元组 进行分组聚合
    val refLog: Array[(String, Int)] = logRDD
      .mapPartitions { iter => iter.map(tuple => (tuple._3, 1)) }
      .reduceByKey { (x, y) => x + y }
      .sortBy((tuple => tuple._2), false)
      .take(10)
    refLog.foreach(println)

    // 当缓存RDD不再使用时，释放资源
    logRDD.unpersist()

    //Thread.sleep(10000000)

    // 应用结束，关闭资源
    sc.stop()
  }

}
