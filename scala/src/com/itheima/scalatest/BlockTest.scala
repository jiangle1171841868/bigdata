package com.itheima.scalatest

object BlockTest {

  def main(args: Array[String]): Unit = {

    var a=10
    var b=10
    //定义变量时用 {} 包含一系列表达式，其中块的最后一个表达式的值就是块的值。
    var c={
      var d=a+b
      var e=a-d
      e
    }
    println(c)
  }

}
