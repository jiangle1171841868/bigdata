package com.itheima.scalaclass

object ObjectTest {

  //todo 实现main方法
  def main(args: Array[String]) {

    if (args.length>0)
      println(args(0)+"hello world")
    else
      println("hello scala")
  }

}

object AppTest extends App{

  //todo 继承 App  可以将写在main中的代码写在这里   也可以接收args参数
    if (args.length>0)
      println(args(0)+"hello world")
    else
      println("hello scala")
}


