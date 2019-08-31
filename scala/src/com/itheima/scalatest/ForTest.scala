package com.itheima.scalatest

object ForTest {

  def main(args: Array[String]) {

    //for循环语法结构：for (i <- 表达式/数组/集合)

    //1.for(i <- 1 to 10) i <- 一个区间
    for (i <- 1 to 10)
      println(i)

    //定义一个数组
    var arr=Array("a","b","c")
    for (i <- arr)
      println(i)

    //高级循环
    //i 1 to 3 外层循环
    //j 1 to 3 内层循环
    //条件 i=j  每个循环都可以带一个条件
    //注意：if前面没有分号
    for (i <- 1 to 3 if i !=1 ; j <- 1 to 3 if i!=j)
      print(10*i+j+" ")
    println()

    //for推导式：如果for循环的循环体以yield开始，则该循环会构建出一个集合
    var x=for (i <- 1 to 10) yield i
    println(x) //打印 Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    //对field操作
    var y=for (i <- 1 to 10) yield i*10
    println(y) //Vector(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)


  }
}
