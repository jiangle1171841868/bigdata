package com.itheima.scalaclass

class Person2 {}
class Student2 extends Person2{}

object Student2{

  def main(args: Array[String]) {

    //todo 创建Student2对象  实例化子类对象 赋值给父类类型的变量
    val p1:Person2=new Student2

    val p2=null

   /* //todo 对象为null 判断一定是false
    println(p2.isInstanceOf[Student2]) //false

    //todo 判断p1 是否是Student的实例
    if(p1.isInstanceOf[Student2]) {

      //todo 是student的实例 就转化为Student
      p1.asInstanceOf[Student2] //com.itheima.scalaclass.Student2@491cc5c9
      println(p1)

    }*/
      //todo 判断p1是否是Person2的实例  只要是  该类和子类的实例都是返回true
      println(p1.isInstanceOf[Person2]) // todo true

      //todo 判断p1的类型是否是Person2类  new的谁就是谁的类型 与接收的父类无关
      println(p1.getClass==classOf[Person2]) // todo false

      //todo 判断p1的类型是否是Student2的类
      println(p1.getClass==classOf[Student2]) //todo true
    }

}