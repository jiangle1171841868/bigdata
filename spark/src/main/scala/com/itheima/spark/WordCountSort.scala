package com.itheima.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCountSort {

  def main(args: Array[String]): Unit = {

    //todo 1.构建spark程序的入口 saprkContext对象

    val sparkConf: SparkConf = new SparkConf().setAppName("wordCountSort").setMaster("local[2]")
    val sc: SparkContext = new SparkContext(sparkConf)

    //todo 2.读取数据
    val inputRDD: RDD[String] = sc.textFile("/datas/sparkInput/wc.txt")

    //todo 3.调用函数处理数据
    val wordsConnt: RDD[(String, Int)] = inputRDD.flatMap(_.trim.split(" ")).map((_, 1)).reduceByKey(_ + _)
    wordsConnt.foreach(println)
    println("================================================================================")

    //todo 排序1 需求:按照词频倒叙排序
    //1.将sortByKey 按照key进行倒叙排序
    //将二元组中的key和value互换 对key排序
    //wordsConnt.map(tuple => tuple.swap)
    wordsConnt.map(tuple => (tuple._2, tuple._1))
      //参数:ascending(升序): Boolean = true   true就是升序 false就是降序
      //隐式转换  RDD类型可以直接调用Order的sortByKey方法  隐式转换方法 放在伴生对象里面
      .sortByKey(ascending = false)
      //获取前三个
      .take(3)
      //排序后 将二元组换回来
      .map(tuple => (tuple._2, tuple._1))
      .foreach(println)
    println("================================================================================")

    //todo 排序2 sortBy指定字段排序
    // 参数: f: (T) => K, 排序的字段 不同的RDD数据集调用sortBy方法  戳进源码  泛型(T)是对应数据集的形式
    //      ascending: Boolean = true,  怎么排序    有默认值参数就可以不写 参数=值
    wordsConnt.sortBy(_._2, false).take(3).foreach(println)
    println("================================================================================")

    //todo 排序3 使用top函数
    // def top(num: Int)
    // (implicit ord: Ordering[T]) 戳进隐式参数的类  val Ordering = scala.math.Ordering  戳等号右边的Ordering  发现是trait Ordering[T]  找Ordering的创建
    // : Array[T] = withScope {
    //    takeOrdered(num)(ord.reverse)
    //  }
    //  柯里化函数 参数列表1:取top几  参数列表2:隐式参数 对谁进行排序
    wordsConnt.top(3)(Ordering.by(_._2)).foreach(println)

    // TODO: 5、应用结束，关闭资源
    if (!sc.isStopped) sc.stop()

  }

}
