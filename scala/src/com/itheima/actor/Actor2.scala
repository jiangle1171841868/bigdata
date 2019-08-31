package com.itheima.actor

import scala.actors.Actor

class Actor2 extends Actor {

  override def act(): Unit = {

    //todo 循环接收消息
    while (true) {
      receive {
        //匹配
        case "start" => println(s"starting...")
        case "end" => println(s"ending")
          //0 正常退出 其他数字 非正常退出
        case "exit"=>System.exit(0)
      }
    }
  }
}

object Actor2 extends App{

  //创建实例
  private val actor = new Actor2

  //调用start方法
  actor.start()

  //向本身actor实例 发送信息
  actor ! "start"
  actor ! "end"
  actor ! "exit"

}

