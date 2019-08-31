package com.itheima.scalaclass

object Test1 {

  def main(args: Array[String])={

    //todo  直接new 对象
    val p1 = new Person

    //todo 通过object中的属性获取对象
    val p2 = Person.person

    //todo  通过object中的方法获取对象
    val p3 = Person.getPerson()

    //打印地址
    println(p1) //com.itheima.scalaclass.Person@23223dd8
    println(p2) //com.itheima.scalaclass.Person@4ec6a292
    println(p3) //com.itheima.scalaclass.Person@4ec6a292

  }
}
