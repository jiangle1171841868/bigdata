package com.itheima.scalatest

// 可变 不可变是由导入的包决定
import scala.collection.immutable.Map

object ImmutableMapTest {
  def main(args: Array[String]) {

    //定义不可变map  不可修改   修改就报错  可变 不可变是由导入的包决定
    //方式1
    //格式Map(key -> value)
    val map1 = Map("name" -> "刘能", "age" -> 30)
    println(map1) //Map(name -> 刘能, age -> 30)

    //方式2
    val map2 = Map(("name", "刘能"), ("age", 30))
    println(map2) //Map(name -> 刘能, age -> 30)

    //操作map
    //显示所有的key
    //方式1
    val keys = map2.keys
    println(keys) //Set(name, age)

    //方式2
    val set = map2.keySet
    println(set) //Set(name, age)

    //根据key获取value
    println(map2("name"))

    //根据key获取value 有value就返回 没有就返回设置的默认值
    println(map2.getOrElse("address", "上海"))

  }
}
