package com.itheima.actor

import scala.actors.Actor

//todo 使用样例类 封装消息  来进行发送和接收
case class SyncMessage(id: Int, msg: String) //发送同步消息
case class AsyncMessage(id: Int, msg: String) //发送异步消息
case class ReplyMessage(id: Int, msg: String) //接收异步消息的结果  状态信息

class Actor4 extends Actor {
  override def act(): Unit = {

    loop {
      react {
        //todo 同步消息
        case AsyncMessage(id, msg) => {
          println(s"这是异步消息...id->$id msg->$msg")

          //todo 发送消息 !!发送异步消息 才有返回值
          //todo sender 发送返回值
          sender ! ReplyMessage(3, "这是异步消息的返回值...")
        }
        case SyncMessage(id, msg) => {
          println(s"这是同步消息...id->$id msg->$msg")

          //todo 发送返回值
          sender ! ReplyMessage(1, "这是同步消息的返回值...")
        }
        case "stop" =>{
          System.exit(0)
        }
      }
    }
  }
}

object Actor4 {

  def main(args: Array[String]): Unit = {

    val actor = new Actor4
    actor.start()

    //todo 发送同步消息 等待返回结果  发送的是样例类 相当于javabean 用于数据传输
    val reply1 = actor !? SyncMessage(1, "这是同步消息,等待返回值...")
    println(reply1)

    //todo 发送异步消息 没有返回值
    actor ! AsyncMessage(2, "这是异步消息,没有返回值...")

    //todo 发送异步消息 有返回值Future[Any]
    val reply = actor !! AsyncMessage(3, "这是异步消息,有返回值...")

    val value = reply.apply()

    println(value)

    actor ! "stop"


  }
}