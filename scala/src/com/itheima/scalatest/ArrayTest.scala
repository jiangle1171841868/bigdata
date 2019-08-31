package com.itheima.scalatest

import scala.collection.mutable.ArrayBuffer

object ArrayTest {

  def main(args: Array[String]) {


    //Array定长数组
    //ArrayBuffer:变长数组 数组缓冲
    //定义一个长度为8的定长数组 使用new的方式 会调用一个applywei数组赋初始值0
    val ints = new Array[Int](10)

    println(ints)

    //toBuffer 将数组转化为数组缓冲 遍历
    println(ints.toBuffer) //ArrayBuffer(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    //for循环遍历
    //for(i <- ints)
     // println(i)


    //定义有初始值的定长数组
    var arr=Array("a","b","c") //ArrayBuffer(a, b, c)
    println(arr.toBuffer)

    //定义一个变长数组 不用指定长度
    val arr2= new ArrayBuffer[Int]()
    println(arr2.toBuffer) //ArrayBuffer() 空

    //向数组尾部追加元素
    //1.+=追加一个或多个元素
    arr2+=1
    println(arr2.toBuffer) //ArrayBuffer(1)

    arr2+=(2,3,4,5,6)
    println(arr2.toBuffer) //ArrayBuffer(1, 2, 3, 4, 5, 6)

    //2.++=追加一个数组 或者缓冲数组
    arr2++=Array(7,8,9)
    println(arr2.toBuffer)

    arr2++=ArrayBuffer(10,11,12)
    println(arr2.toBuffer)

    //插入数据 insert  在索引位置插入一个或多个数据  索引从0开始
    arr2.insert(0,88,99,100) //ArrayBuffer(88, 99, 100, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    println(arr2.toBuffer)

    //按照索引删除元素
    arr2.remove(0)
    println(arr2.toBuffer)




  }

}
