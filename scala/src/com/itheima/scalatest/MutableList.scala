package com.itheima.scalatest

import scala.collection.mutable.ListBuffer

object MutableList {

  def main(args: Array[String]): Unit = {

    //构建一个可变集合
    val list0 = ListBuffer[Int](1, 2, 3)

    //创建一个空的可变列表
    val list1 = new ListBuffer[Int]

    //向list中追加新的元素
    list1 += 4
    println(list1) //ListBuffer(4)
    list1.append(3)
    println(list1) //ListBuffer(4, 3)


    //将list1的元素追加到list0中 没有新集合生成
    list0 ++= list1
    println(list0) //ListBuffer(1, 2, 3, 4, 3)
    println(list1) //ListBuffer(4, 3)

    //有新集合生成
    val list = list0 ++= list1
    println(list) //ListBuffer(1, 2, 3, 4, 3)

    //在list0后面追加新元素 生成一个新集合
    val list2 = list :+ 10
    println(list2) //ListBuffer(1, 2, 3, 4, 3, 4, 3, 10)

    //删除元素
    list0 -= 10
    println(list0) //ListBuffer(1, 2, 3, 4, 3, 4, 3)

    //删除一个集合列表
    val list3 = list0 -- List(1, 2)
    println(list3) //ListBuffer(3, 4, 3, 4, 3)


    //将可变list集合转化为不可变
    var list4 = list3.toList

    //将可变list转化为数组
    var list5 = list0.toArray
    println(list5.toBuffer) //ArrayBuffer(1, 2, 3, 4, 3, 4, 3)

    val builder: StringBuilder = new StringBuilder
    val builder2: StringBuilder = list.addString(builder, "---")
    println(builder2)

  }

}
