package com.itheima.scalaclass

object CaseTest02 {

  def main(args: Array[String]) {

    val student: Person2 = new Student2

    //todo 匹配模式
    student match {

      case s: Person2 => println("person类型的类...")

      case _ => println("没有类型匹配...")
    }

  }
}
