package com.itheima.scalatest

import scala.collection.immutable._

object ImMutableList {
  def main(args: Array[String]): Unit = {

/*
    /* //不可变list集合
     //字符串list
     val strs: List[String] = List("hello", "scala", "java")
     println(strs) //List(hello, scala, java)

     //整型list
     val nums: List[Int] = List(1, 2, 3, 4, 5)
     println(nums) //List(1, 2, 3, 4, 5)

     //空集合
     val empty: List[Nothing] = List()
     println(empty) //List()*/

    //构建list集合的两个基本元素Nil和::  Nil表示一个空列表
    val strs = "hello" :: "scala" :: "java" :: Nil
    println(strs) //List(hello, scala, java)

    val nums = 1 :: (2 :: (3 :: (4 :: Nil)))
    println(nums)

    val nums2 = 1 :: 2 :: 3 :: 4 :: Nil
    println(nums2) //List(1, 2, 3, 4)

    //空list
    val empt = Nil
    println(empt)


    //获取集合的第一个元素
    println(nums.head) //1

    //获取集合元素 除了第一个
    println(nums.tail) //List(2, 3, 4)

    //将0插入到list集合前面生成一个新集合
    println(nums) //List(1, 2, 3, 4)
    val list1 = 0 :: nums
    println(list1) //List(0, 1, 2, 3, 4)
    val list2 = nums.::(1)
    println(list2) //List(1, 1, 2, 3, 4)
    val list3 = 2 +: nums
    println(list3) //List(2, 1, 2, 3, 4)
    val list4 = nums.+:(3)
    println(list4)  //List(3, 1, 2, 3, 4)

    //在list1集合后面插入一个元素
    val list5 = list1:+3
    println(list5) //List(0, 1, 2, 3, 4, 3)
*/

//todo 高亮
    val list10=1::2::3::Nil
    val list11 =4::5::6::Nil

    //在你前面集合的尾部添加后面的集合
    //在列表的尾部添加另外一个集合   以前面的集合为标准
    val list12=list10++list11
    println(list12) //List(1, 2, 3, 4, 5, 6)

    val list13=list11++list10
    println(list13) //List(4, 5, 6, 1, 2, 3)

    //在集合的尾部添加一个集合  以前面的集合为标准
    val list14=list10:::list11
    println(list14) //List(1, 2, 3, 4, 5, 6)

    val list16=list11:::list10
    println(list16) //List(4, 5, 6, 1, 2, 3)

    //在集合的尾部添加一个集合 以前面为标准
    val list15=list10++:list11
    println(list15) //List(1, 2, 3, 4, 5, 6)


  }


}
