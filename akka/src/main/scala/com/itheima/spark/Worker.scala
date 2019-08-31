package com.itheima.spark

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._

class Worker(val memory: Int, val cores: Int, val masterHost: String, val masterPort: String) extends Actor {

  val workerID = UUID.randomUUID().toString

  //todo _   表示null 全局变量  需要在preStart中赋值   需要定义成var类型
  var master: ActorSelection = _

  //定义发送心跳的初始时间 和 时间间隔
  val SEND_HEART_HEAT_START = 0
  val SEND_HEART_HEAT_INTERVAL = 5000 //5秒

  //todo 初始化方法 携带信息 向master注册
  override def preStart(): Unit = {

    // todo 获取master的引用地址 通过全局对象context获取
    // todo 方法需要一个path路径：1、通信协议、2、master的IP地址、3、master的端口 4、创建master actor老大 5、actor层级
    master = context.actorSelection(s"akka.tcp://masterActorSystem@$masterHost:$masterPort/user/masterActor")

    //todo 向master发现注册消息
    master ! RegisterMessage(workerID, memory, cores)
  }

  override def receive: Receive = {

    //todo 接收注册成功的信息 接收到消息之后 开始每隔5s心跳一次 因为暂时(类 方法)只能先发送定时信息给自己
    // 所以先发送给自己 自己接收到之后 在发送给master
    case ReplyMessage(message) => {

      //打印信息
      println(message)

      //todo 5秒发送心跳给自己
      //todo 需要手动导入隐式转换  不然没有方法
      //todo 需要导包 import scala.concurrent.duration._
      import context.dispatcher
      context.system.scheduler.schedule(SEND_HEART_HEAT_START millis, SEND_HEART_HEAT_INTERVAL millis, self, HeartBeat)
    }
    //todo 接定时信息 向master发送心跳
    case HeartBeat => {
      master ! SendHeartBeat(workerID)
    }
  }
}

object Worker {

  def main(args: Array[String]): Unit = {

    //todo 定义worker需要的变量
    val host = args(0)
    val port = args(1)
    val memory = args(2).toInt
    val cores = args(3).toInt

    //todo master的ip和端口号  获取master对象 发送消息
    val masterHost = args(4)
    val masterPort = args(5)


    //todo 创建配置对象信息
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |
      """.stripMargin

    //todo 解析string类型的配置信息
    val config: Config = ConfigFactory.parseString(configStr)

    //todo 创建actorSystem对象
    val workActorSystem: ActorSystem = ActorSystem.create("workActorSystem", config)

    //todo 通过workActorSystem创建workActor对象
    // 在preStart方法中需要获取master的地址  来发送消息 所以需要master的端口和端口信息  就在main方法里面    构造函数中传入
    val workerActor: ActorRef = workActorSystem.actorOf(Props(new Worker(memory, cores, masterHost, masterPort)), "workerActor")
  }
}