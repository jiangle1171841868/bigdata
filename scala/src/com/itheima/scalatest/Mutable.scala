package com.itheima.scalatest

//
import scala.collection.mutable._

object Mutable {

  def main(args: Array[String]) {

    //定义可变的map集合
    var map = Map("刘能" -> 30, "赵四" -> 30)
    println(map)

    //添加键值对
    map += ("谢广坤" -> 30)
    println(map)

    //添加多个键值对
    map += ("张三" -> 30, "李四" -> 30)
    println(map)
    /*
        //更新键值对
        //有等于就更新  没有就获取
        map("刘能") = 40
        println(map("刘能"))

        /*//更新多个键值对  就是插入替换
        map += ("刘能" -> 50, "赵四" -> 20)
        println(map)*/

        //删除key
        map -= ("张三")
        map.remove("李四")
        println(map)

        //遍历map
        //1.通过key 遍历
        for (i <- map.keys) println(i + "->" + map(i))

        //2.模式匹配遍历
        for ((x, y) <- map) println(x + "->" + y)

        //3.foeeach遍历
        map.foreach { case (x, y) => println(x + "->" + y) }


        println(map.count(_._2 == 30))*/

    //todo def filter(p: A => Boolean)
    map.filter(_._2 == 30).filter(_._1 == "刘能").foreach (print)
  }

}
