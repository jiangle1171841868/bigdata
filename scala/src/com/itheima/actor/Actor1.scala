package com.itheima.actor

import scala.actors.Actor

/**
  * 1.创建一个类继承Actot特质  重写act方法
  * 2.调用start方法 执行actor
  * 3.向actor发送消息 ! !? !!
  * 4.act 方法执行完成之后，程序会调用 exit 方法
  */
class Actor1 extends Actor{

  override def act(): Unit = {

    //接收消息
    receive{ // todo 只能接收一次  想要接收多次使用循环
      //匹配
      case "start"=>println(s"starting...")
      case "end"=>println(s"ending")
    }

  }
}

object Actor1{

  def main(args: Array[String]): Unit = {
    //创建一个实例
    val actor = new Actor1
    //调用start方法
    actor.start()
    //向本身actor 发送消息
    actor ! "start"
    actor ! "end"

  }
}
