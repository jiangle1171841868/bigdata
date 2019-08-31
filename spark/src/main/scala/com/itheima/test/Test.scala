package com.itheima.test

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

object Test {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[2]")
    val sc: SparkContext = new SparkContext(conf)

    // TODO: 2、采用并行化方式创建RDD
    val inputRDD: RDD[Int] = sc.parallelize(1 to 10, numSlices = 2)


    //todo def fold(zeroValue: T)(op: (T, T) => T): T 柯里化函数 参数类型会根据RDD的数值变化成具体类型
    // 第一个参数列表:一个参数  初始值
    // 第二个参数列表:参数类型 函数   输入 两个 输出一个
    //todo 结果分析  fold函数
    // 第二个参数列表     分区计算的时候  : (第一次第一个值是初始值,后面就是计算的变量)   第二个值是集合的元素
    //                  聚合计算的时候  :  第一个值永远是初始值  第二个值是分区计算的结果
    inputRDD.fold(100)((x, y) => {
      println(s"x=$x y=$y $x+$y=${x + y}")
      x + y //函数的返回值
    })

    //todo def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U
    //todo 参数说明
    // 第一个参数列表:初始值
    // 第二个参数列表:第一个参数:函数  输入两个 输出一个 对每个分区聚合的函数 第一个数据   第一次是初始值 以后就是计算的值  只会有一次初始值
    //              第二个参数:函数 输入两个输出一个   全局聚合 对每个分区的数据进行聚合  分区的值直接聚合 不用再有初始值
    inputRDD.aggregate(100)(
      (x, y) => {
      println(s"x=$x y=$y $x+$y=${x + y}")
      x + y //函数的返回值)
    },
      ((a, b) => {
      println(s"a=$a b=$b $a+$b=${a + b}")
      a + b
    }))

    /* inputRDD.aggregate(new ListBuffer[Int]())
     (
       // todo // seqOp: (U, T) => U
       (u: ListBuffer[Int], t: Int) => {

         //todo 将分区中的每一个元素添加到list集合中
         u += t
         //对list降序排列 获取最大的两个
         u.sortBy(-_).take(2)
       },

       //todo combOp: (U, U) => U
       ((u1: ListBuffer[Int], u2: ListBuffer[Int]) => {
         //将两个list合并
         u1 ++= u2
         //倒叙排列 获取top2
         u1.sortBy(-_).take(2)
       })
     )*/


  }
}
