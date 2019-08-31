package com.itheima.scalatest

object TupleTest {

  def main(args: Array[String]): Unit = {

    //元组是不同类型值的聚集 使用括号将不同类型的值括起来
    val t=("hello",123,1000.00,true)

    //获取元组中的值  索引从1开始
    //格式:t._1
    println(t._1)

    //元素迭代
    //productIterator方法获取一个迭代器
    t.productIterator.foreach(i=>println(i))

    t.productIterator.foreach(println(_)) //_ 表示循环的每一个元素



  }
}
