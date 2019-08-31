package com.itheima.test

import java.io.File

import scala.actors.{Actor, Future}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source


//todo 定义样例类
//todo 提交任务的 样例类  参数:文件路径
case class SubmitTask0(filePath: String)

//todo 返回结果  参数:Map(单词->次数)Map[String,Int] 中括号
case class ResultTask0(result: Map[String, Int])

class Task0 extends Actor {
  override def act(): Unit = {

    //循环匹配
    loop {

      //todo 接收信息
      react {
        case SubmitTask0(filePath) => {

          //todo   scala IO 读取文件内容
          val content = Source.fromFile(new File(filePath)).mkString
          println(content)
          println("-------------------------------------------------------------")

          //todo 按照换行符划分  获取每一行的内容 win \r\n, linux下文件的换行符 \n
          val lines: Array[String] = content.split("\\r\\n")
          println(lines.toBuffer)
          println("-------------------------------------------------------------")
          //ArrayBuffer(hadoop hadoop hbase , hive hbase hive impala, hdfs oozie, hive hbase hive impala, spark java java, es es flume sqoop, hive hbase hive impala, spark java java, es es flume sqoop)

          //todo 按照空格分割每一行 获取每个单词  先分割(一个元素生成一个数组) 再压缩生成一个数组
          val words: Array[String] = lines.flatMap(_.split(" "))
          println(words.toBuffer)
          println("-------------------------------------------------------------")
          //ArrayBuffer(hadoop, hadoop, hbase, hive, hbase, hive, impala, hdfs, oozie, hive, hbase, hive, impala, spark, java, java, es, es, flume, sqoop, hive, hbase, hive, impala, spark, java, java, es, es, flume, sqoop)

          //todo 转化为对偶数组 集合中的元素就是元组 每个单词计数为1
          //val wordandone: Array[(String, Int)] = words.map((_, 1))
          val wordAndOne: Array[(String, Int)] = words.map(x => (x, 1))
          println(wordAndOne.toBuffer)
          println("-------------------------------------------------------------")
          //ArrayBuffer((hadoop,1), (hadoop,1), (hbase,1), (hive,1), (hbase,1), (hive,1), (impala,1), (hdfs,1), (oozie,1), (hive,1), (hbase,1), (hive,1), (impala,1), (spark,1), (java,1), (java,1), (es,1), (es,1), (flume,1), (sqoop,1), (hive,1), (hbase,1), (hive,1), (impala,1), (spark,1), (java,1), (java,1), (es,1), (es,1), (flume,1), (sqoop,1))

          //todo 拿到元组中的每一个元素 _._1  使用元组的第一个分组 调用方法_1 获取元组第一个元素
          val wordGroup: Map[String, Array[(String, Int)]] = wordAndOne.groupBy(_._1)

          //todo 统计每个单词出现的次数
          //todo mapValues(_.length) 统计map集合value的长度 转化为一个map
          val result: Map[String, Int] = wordGroup.mapValues(_.length)


          //todo 返回消息给发送方
          sender ! ResultTask0(result)
        }
        case "stop" => System.exit(0)
      }
    }
  }
}

object Task0 {

  def main(args: Array[String]): Unit = {

    //定义一个set集合，用于存放Future
    val replySet = new mutable.HashSet[Future[Any]]()
    //定义一个list集合，用于存放真正的数据
    val taskList = new ListBuffer[ResultTask0]

    //准备数据文件
    val files = Array("F:\\wordcount1.txt","F:\\wordcount2.txt","F:\\wordcount3.txt")

    //遍历文件
    for (file <- files) {

      //创建一个实例
      val task = new Task

      //调用start
      task.start()

      //发送消息  获取结果
      val reply: Future[Any] = task !! SubmitTask0(file)

      //将结果添加到set集合
      replySet += reply

    }

    //遍历set集合
    while (replySet.size > 0) {

      //过滤出真正有数据的set  返回的future是状态 可能有数值 可能没有 需要使用isSet方法 判断可用的数据  就是有值得数据
      val toCompleted: mutable.HashSet[Future[Any]] = replySet.filter(_.isSet)

      //遍历toCompleted 获取数据
      for (t <- toCompleted) {
        val value: Any = t.apply()
        //将数据添加到list集合里面
        taskList += value.asInstanceOf[ResultTask0]

        //将set集合中已经获取结果的 数据移除
        replySet -= t
      }
    }
    //println(taskList.map(_.result).flatten.groupBy(_._1).mapValues(x=>x.foldLeft(0)(_+_._2)))
    println(taskList)
    //ListBuffer(ResultTask0(Map(impala -> 3, sqoop -> 2, java -> 4, oozie -> 1, hadoop -> 2, spark -> 2, hive -> 6, es -> 4, flume -> 2, hbase -> 4, hdfs -> 1)))
    println("-------------------------------------------------------------")
    // todo 获取集合中的结果ResultTask0对象 获取result: Map[String, Int]
    println(taskList.map(_.result))
    //ListBuffer(Map(impala -> 3, sqoop -> 2, java -> 4, oozie -> 1, hadoop -> 2, spark -> 2, hive -> 6, es -> 4, flume -> 2, hbase -> 4, hdfs -> 1))
    println("-------------------------------------------------------------")
    //todo 压扁 成一个list集合
    println(taskList.map(_.result).flatten)
    //ListBuffer((impala,3), (sqoop,2), (java,4), (oozie,1), (hadoop,2), (spark,2), (hive,6), (es,4), (flume,2), (hbase,4), (hdfs,1))
    println("-------------------------------------------------------------")
    //todo 按照key进行分组
    println(taskList.map(_.result).flatten.groupBy(_._1))
    //Map(impala -> ListBuffer((impala,3)), sqoop -> ListBuffer((sqoop,2)), java -> ListBuffer((java,4)), oozie -> ListBuffer((oozie,1)), hadoop -> ListBuffer((hadoop,2)), spark -> ListBuffer((spark,2)), hive -> ListBuffer((hive,6)), es -> ListBuffer((es,4)), flume -> ListBuffer((flume,2)), hbase -> ListBuffer((hbase,4)), hdfs -> ListBuffer((hdfs,1)))
    println("-------------------------------------------------------------")
    //todo 获取map集合value的长度 输出key:value长度   x.foldLeft(0)(_+_._2) 第一个是初始值 第二个是元素中的值 元素是元组 获取第二个元素求和
    println(taskList.map(_.result).flatten.groupBy(_._1).mapValues(x => x.foldLeft(0)(_ + _._2)))
    //Map(impala -> 3, sqoop -> 2, java -> 4, oozie -> 1, hadoop -> 2, spark -> 2, hive -> 6, es -> 4, flume -> 2, hbase -> 4, hdfs -> 1)

    //创建一个实例
    val task = new Task

    //调用start
    task.start()

    //发送消息  获取结果
    task !! "stop"


  }
}
