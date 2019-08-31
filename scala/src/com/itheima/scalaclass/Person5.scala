package com.itheima.scalaclass

class Person5 {}

class Person6 {}

class Student5 extends Person5

object Student5 {
  def main(args: Array[String]) {

    val p: Person5 = new Student5

    p match {
      //匹配是否是Person5类或者子类对象
      case per: Person5 => println("This is a Person5's Object!")

      //todo 匹配剩余的情况
      case _ => println("Unknown type!")

    }
  }
}

