package com.itheima.scalatest

//遍历数组
object ArrayTest2 {

  def main(args: Array[String]) {

    var arr=Array(1,2,3,4,5)

    //for循环遍历
    //for(i <- arr)
    //println(i)

    //0 until 10 返回一个区间 包含0不包含10  左闭右开
    //for(i <- 0 until arr.length)
    //  println(i) //输出0-4


    //to  左闭右闭
    // for(i<- 0 to arr.length)
    // println(i)

    //结果反转输出
    for(i <- (0 to arr.length).reverse)
     println(i)



  }


}
