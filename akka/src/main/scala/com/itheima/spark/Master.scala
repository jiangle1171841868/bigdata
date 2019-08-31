package com.itheima.spark

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

//todo 创建一个Master继承akka的Actor
class Master extends Actor {

  //todo map集合 key:workerID value:WorkerInfo(注册信息)
  private val workerInfoMap = new mutable.HashMap[String, WorkerInfo]()

  //todo 保存WorkerInfo(注册信息)
  private val workerInfoList = new ListBuffer[WorkerInfo]()

  //定义检查心跳的初始时间 和 时间间隔
  val SEND_HEART_HEAT_START = 0
  val SEND_HEART_HEAT_INTERVAL = 8000 //10秒
  //超时时间
  val CHECK_OUT_TIME_INTERVAL = 10000

  //todo 重写preStart()方法 初始化方法 在构造代码块执行之后执行  就是实例构造完成后执行
  override def preStart(): Unit = {

    //todo 向自己发送超时检查
    //todo 需要导包 import scala.concurrent.duration._
    import context.dispatcher
    context.system.scheduler.schedule(SEND_HEART_HEAT_START millis, SEND_HEART_HEAT_INTERVAL millis, self, CheckOutTime)
  }

  //todo receive会在prestart方法执行之后被调用 有消息就会调用receive方法
  override def receive: Receive = {

    //todo 接收注册消息 模式匹配
    case RegisterMessage(workerID, memory, cores) => {

      //todo 判断是否已经注册过 注册过之后 就不用注册  没有注册开始注册
      if (!workerInfoMap.contains(workerID)) {

        //todo 接收之后 将样例类的数据取出 保存在WorkerInfo(scalaBean)中保存到map集合(判断是否已经注册)和list集合中(来判断是否超时和内存排序)
        //将数据取出保存到workerInfo中
        val workerInfo = new WorkerInfo(workerID, memory, cores)

        //保存到map和list集合
        workerInfoMap.put(workerID, workerInfo)
        workerInfoList += workerInfo

        //todo 注册成功之后 给worker发送消息
        sender ! ReplyMessage(s"$workerID 注册成功了...")
      }
    }

    //todo 接收到心跳信息 将当前时间设置为worker的上一次心跳时间
    case SendHeartBeat(workerID) => {

      //todo 判断workID是否注册
      if (workerInfoMap.contains(workerID)) {
        val workerInfo: WorkerInfo = workerInfoMap(workerID)

        //获取系统时间
        val lastTime: Long = System.currentTimeMillis()

        //将workInfo的上一次心态时间设置为当前系统时间
        workerInfo.lastHeartBeatTime = lastTime
      }
    }

    //todo 检查是否超时 超时就将数据从map和list中移除
    case CheckOutTime => {
      //todo 过滤出超时的worher 当前系统时间-上一次心跳时间>10s 就是超时 就将数据从map和list中移除
      val infoes: ListBuffer[WorkerInfo] = workerInfoList.filter(x => System.currentTimeMillis() - x.lastHeartBeatTime > CHECK_OUT_TIME_INTERVAL)
      //遍历超时集合 移除
      for (workerInfo <- infoes) {
        workerInfoList -= workerInfo
        workerInfoMap -= workerInfo.workerID
        println("超时的workerId:" + workerInfo.workerID)
      }

      println("活着的worker总数：" + workerInfoList.size)

      //master按照worker内存大小进行降序排列
      println(workerInfoList.sortWith(_.memory > _.memory))
    }
  }
}

object Master {

  def main(args: Array[String]): Unit = {

    //todo 定义配置对象需要的变量 端口 ip
    val host = args(0)
    val port = args(1)

    //todo 准备配置文件  按住shift 按三下冒号 松开shift 回车  在引号最外边添加s
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    //todo 解析字符串配置  可以解析 string map 文件等
    val config: Config = ConfigFactory.parseString(configStr)

    //todo 创建actorSystem对象  通过actorSystem  创建 管理 监督 actor
    //todo 第一个参数:actorSystem的名字  第二个参数:配置对象
    val masterActorSystem: ActorSystem = ActorSystem.create("masterActorSystem", config)

    //todo 创建actor
    val masterActor: ActorRef = masterActorSystem.actorOf(Props(new Master), "masterActor")
  }
}
