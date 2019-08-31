package com.itheima.spark.group

import org.apache.spark.{SparkConf, SparkContext, TaskContext}
import org.apache.spark.rdd.RDD

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object AggregateByKeyTest {
  def main(args: Array[String]): Unit = {
    //todo 1.构建SparkContext对象
    val sc: SparkContext = {
      val sparkConf: SparkConf = new SparkConf()
        //去掉stripSuffix  object类前面有$  使用方法去掉
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[1]")
      SparkContext.getOrCreate(sparkConf)
    }

    //设置日志级别
    sc.setLogLevel("ERROR")

    //todo 1.读取数据
    val inputRDD: RDD[String] = sc.textFile("datas/group/group.data", 3)

    //todo 2.处理数据
    val dataRDD: RDD[(String, ListBuffer[Int])] = inputRDD
      .mapPartitions { iter =>
        iter.map(line => {
          val Array(word, count) = line.trim.split("\\s+")
          //二元组 将value转化为int类型
          (word, count.toInt)
        })
      }

      /**
        * def aggregateByKey[U: ClassTag]
        * (zeroValue: U)
        * (seqOp: (U, V) => U,combOp: (U, U) => U)
        * :RDD[(K, U)]
        */
      .aggregateByKey(new mutable.ListBuffer[Int]())(
      (u, v) => {
        //println(s"p-${TaskContext.getPartitionId()} u=$u,v=$v,{$u+$v}")
        //查看数据类型
        //val xx: mutable.Seq[Int] = u
        //val xxx: Int = v
        //将value添加到集合  排序  取前三条
        u += v
        u.sortBy(-_).take(3)
      },
      (u1, u2) => {
        //println(s"p-${TaskContext.getPartitionId()} u1=$u1,u2=$u2,{ $u1+$u2}")
        u1 ++= u2
        u1.sortBy(-_).take(3)
      }
    )

    dataRDD.foreachPartition { iter =>
      val xx: Iterator[(String, ListBuffer[Int])] = iter

      //将迭代器转化为list集合 里面是二元组
      val list: List[(String, ListBuffer[Int])] = iter.toList

      //对二元组的第二个元素每一个元素进行操作 遍历出 k v类型的数据
      list.map {
        case (word, counts) =>
          println(counts.map(count => (word, count)))
      }
    }
    sc.stop()
  }

  /**
    * p-0 u=ListBuffer(),v=78,{ListBuffer()+78}
    * p-0 u=ListBuffer(),v=98,{ListBuffer()+98}
    * p-0 u=ListBuffer(78),v=80,{ListBuffer(78)+80}
    * p-0 u=ListBuffer(),v=98,{ListBuffer()+98}
    * p-0 u=ListBuffer(80, 78),v=69,{ListBuffer(80, 78)+69}
    * p-0 u=ListBuffer(98),v=87,{ListBuffer(98)+87}
    * p-1 u=ListBuffer(),v=97,{ListBuffer()+97}
    * p-1 u=ListBuffer(),v=86,{ListBuffer()+86}
    * p-1 u=ListBuffer(),v=97,{ListBuffer()+97}
    * p-1 u=ListBuffer(97),v=78,{ListBuffer(97)+78}
    * p-1 u=ListBuffer(97, 78),v=34,{ListBuffer(97, 78)+34}
    * p-2 u=ListBuffer(),v=85,{ListBuffer()+85}
    * p-2 u=ListBuffer(),v=92,{ListBuffer()+92}
    * p-2 u=ListBuffer(85),v=72,{ListBuffer(85)+72}
    * p-2 u=ListBuffer(92),v=32,{ListBuffer(92)+32}
    * p-2 u=ListBuffer(92, 32),v=23,{ListBuffer(92, 32)+23}
    * p-0 u1=ListBuffer(98, 87),u2=ListBuffer(86),{ ListBuffer(98, 87)+ListBuffer(86)}
    * p-0 u1=ListBuffer(98, 87, 86),u2=ListBuffer(85, 72),{ ListBuffer(98, 87, 86)+ListBuffer(85, 72)}
    * (cc,ListBuffer(98, 87, 86))
    * p-1 u1=ListBuffer(98),u2=ListBuffer(97, 78, 34),{ ListBuffer(98)+ListBuffer(97, 78, 34)}
    * p-1 u1=ListBuffer(98, 97, 78),u2=ListBuffer(92, 32, 23),{ ListBuffer(98, 97, 78)+ListBuffer(92, 32, 23)}
    * (bb,ListBuffer(98, 97, 92))
    * p-2 u1=ListBuffer(80, 78, 69),u2=ListBuffer(97),{ ListBuffer(80, 78, 69)+ListBuffer(97)}
    * (aa,ListBuffer(97, 80, 78))
    */



}
