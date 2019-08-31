package com.itheima.test

import java.io.File

import scala.actors.{Actor, Future}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

//todo 提交任务 参数:路径
case class SubmitTask(fileName: String)

//todo 返回结果 参数:Map[String,Int]  Map(word,count)
case class ResultTask(result: Map[String, Int])

class Task extends Actor {

  override def act(): Unit = {

    loop {
      react {
        case SubmitTask(filePath) => {

          //todo  读取文件
          val content: String = Source.fromFile(new File(filePath)).mkString

          //todo 按照换行符切割 获取每一行的内容 返回Array数组
          val lines: Array[String] = content.split("\r\n")
          //println(lines.toBuffer)

          //todo 使用map方法 对Array数组中的每一个元素处理 split处理每一个元素 一个元素切割后获取数组
          //todo 处理之后是Array[Array[String]]  使用flatted方法压平
          val words: Array[String] = lines.flatMap(_.split(" "))
          //println(words.toBuffer)

          //todo 使用map对每一个元素进行操作 将每一个单词标记为1 返回一个元组(word,1)
          val wordAndOne: Array[(String, Int)] = words.map((_, 1))
          //println(wordAndOne.toBuffer)

          //todo 使用groupBy进行分组 一个参数是分组的元素  按照元组中的第一个元素进行分组 元组获取第一个元素_1
          // 返回一个map集合  key为分组的元素  value是 元素组成的数组
          val groupMap: Map[String, Array[(String, Int)]] = wordAndOne.groupBy(_._1)
          // todo 快捷键 groupMap.for    elem代表map中的一个元素 就是元组
          //for (elem <- groupMap) {println(elem._1,elem._2.toBuffer)}

          //todo 统计单词的个数 对value中的第二个元素进行求和 获取出现的次数
          //todo 首先获取map集合的value groupMap.mapValues
          //todo  对value(元组)进行操作  对第二个值进行累加   foldLeft(初始值)(初始值 + 表达式要操作的元素)
          //val countMap: Map[String, Int] = groupMap.mapValues(_.foldLeft(0)(_ + _._2))
          //todo 因为map集合中的value(Array数组)的第二个元素都是1 所以直接统计数组的长度 就是相加的值
          val result: Map[String, Int] = groupMap.mapValues(_.length)
          /*for (elem <- result) {
            println(elem._1 + " -> " + elem._2)
          }*/
          //println(result)
          //todo 将结果封装到ResultTask中返回
          sender ! ResultTask(result)
        }
        case "stop" => System.exit(0)
      }
    }
  }
}

object Task {

  def main(args: Array[String]): Unit = {

    val futureSet: mutable.HashSet[Future[Any]] = new mutable.HashSet[Future[Any]]()

    //todo ListBuffer[ResultTask]() 要加括号
    val completeResultList = new ListBuffer[ResultTask]()

    //准备数据文件
    val filePaths = Array("F:\\wordcount1.txt", "F:\\wordcount2.txt", "F:\\wordcount3.txt")

    //遍历文件
    for (filePath <- filePaths) {

      val task: Task = new Task

      task.start()

      //发送有返回值的异步消息 返回值是状态 Future[Any]   不一定有结果
      val reply: Future[Any] = task !! SubmitTask(filePath)

      //println(reply)
      //返回值封装到集set集合里面
      futureSet += reply

    }

    //获取有futureSet中 有数据的future 取出数据 封装到list集合中 使用isSet判断
    if (futureSet.size > 0) {

      //todo p: A => Boolean  filter 对元素进行过滤 过滤出符合条件的元素
      val replySet: mutable.HashSet[Future[Any]] = futureSet.filter(_.isSet)

      //遍历获取数据
      for (reply <- replySet) {
        //todo 注意使用apply获取数据之后是Any类型的  我们明确知道数据是ResultTask类型的所以可以转化为ResultTask
        //val completeResult: Any = reply.apply()
        val completeResult: ResultTask = reply.apply().asInstanceOf[ResultTask]

        //将结果添加到list集合
        completeResultList += completeResult

        //将完成后有数据 并且将数据取出的从set集合移除
        futureSet -= reply
      }
    }
    //todo 获取结果
    //todo List[ResultTask(result: Map[String, Int]]
    println(completeResultList.map(_.result.groupBy(_._1).mapValues(_.foldLeft(0)(_ + _._2))))
    //println(completeResultList.map(_.result))

    val task: Task = new Task

    task.start()

    task !! "stop"
  }


}