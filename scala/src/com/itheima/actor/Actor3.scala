package com.itheima.actor

import scala.actors.Actor

//todo 使用react 代替 receive react可以复用线程 循环 不能用while 用loop
class Actor3 extends Actor{
  override def act(): Unit ={

    loop{

      react{
        //匹配
        case "start" => println(s"starting...")
        case "end" => println(s"ending")
        //0 正常退出 其他数字 非正常退出
        case "exit"=>System.exit(0)
      }
    }
  }
}

object Actor3 extends App{

  //创建实例
  private val actor = new Actor2

  //调用start方法
  actor.start()

  //向本身actor实例 发送信息
  actor ! "start"
  actor ! "end"
  actor ! "exit"

}

