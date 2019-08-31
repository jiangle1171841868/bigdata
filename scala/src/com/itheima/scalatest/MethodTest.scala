package com.itheima.scalatest

object MethodTest {

  def main(args: Array[String]) {

    //调用方法
    val a = m1(10, 20)
    println(a)

    //将函数作为参数 传入到方法中
   println(m3(f1))

  }

  //定义一个方法
  //def 方法名(参数名:类型,参数名:类型):返回值类型=方法体
  //Unit 表示没有返回值
  //方法体的最后一个表达式就是方法的返回值
  def m1(a: Int, b: Int): Int = a + b

  def m2(a: Int, b: Int): Int =
    if (a > 10) a + b
    else a - b

  //定义一个函数 函数是带有参数的表达式
  //格式(参数:类型,参数:类型)=> 表达式
  //var add = (x: Int, y: Int) => x + y

  //定义一个特殊方法 m3
  //参数是一个函数:要求是输入两个int类型 输出一个int类型的函数
  def m3(f: (Int, Int) => Int) = f(10, 20)

  //定义一个函数f1 满足 两个Int输入 一个Int输出
  var f1 = (x: Int, y: Int) => x + y


}
