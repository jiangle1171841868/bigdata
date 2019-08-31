package com.itheima.scalatest

object WhileTest {

  def main(args: Array[String]) {

    //while循环
    //如果不符合条件可能一次都不会执行
    var a:Int=1;

    while (a<5){

      println(a)

      a=a.+(1)
    }
  }
}
