package com.itheima.scalatest

object ArrayTest3 {

  def main(args: Array[String]) {

    /*   //定义一个数组
       val arr = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

       //将偶数取出乘以10后再生成一个新的数组
       //一个循环可以加一个条件   使用yield生成新的新的数组
       var arr2=for(i <- arr if (i % 2) == 0 ) yield i*10
       println(arr2.toBuffer) //ArrayBuffer(20, 40, 60, 80)

       //filter过滤 将返回值为true的元素留下来
       //map将数组元素取出来  _ 相当于元素
       //先使用filter过滤出奇数  再使用map获取元素 乘10
       val arr3 = arr.filter(_ % 2 != 0).map(_ * 10)

       println(arr3.toBuffer) //ArrayBuffer(10, 30, 50, 70, 90)

       //数组求和
       println(arr.sum) //45

       //求最大值
       println(arr.max) //9

       //最小值
       println(arr.min) //1

       //排序
       println(arr.sorted.toBuffer) //ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9)

   */

    //将偶数组转化为map
    val arr4 = Array(("zhangsan", 1), ("lisi", 2))
    //转化为map
    val map = arr4.toMap
    println(map) //Map(zhangsan -> 1, lisi -> 2)


    /*val names = Array("刘能", "赵四", "谢广坤")
    val ages = Array("40", "50", "45")

    //使用拉链操作(zip) 将多个值捆绑在一起 生成新数组
    val tuples = names.zip(ages)
    println(tuples.toBuffer) //ArrayBuffer((刘能,40), (赵四,50), (谢广坤,45))*/


   /* //数组长度不一样  生成的新数组 为较小数组元素的个数
    val names = Array("刘能", "赵四", "谢广坤")
    val ages = Array("40", "50")

    val tuples = names.zip(ages)
    println(tuples.toBuffer) //ArrayBuffer((刘能,40), (赵四,50))*/

     //数组长度不一样  生成的新数组 为较小数组元素的个数
    //数组长度不一致  使用zipAll 为元素少的默认填充
    val names = Array("刘能", "赵四", "谢广坤")
    val ages = Array("40", "50")

    //(ages,"王老七",30) 对names添加元素  ages添加元素
    val tuples = names.zipAll(ages,"王老七",30)
    println(tuples.toBuffer) //ArrayBuffer((刘能,40), (赵四,50), (谢广坤,30))




  }

}
