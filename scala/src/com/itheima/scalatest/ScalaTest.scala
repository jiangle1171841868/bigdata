package com.itheima.scalatest

object ScalaTest {

  def main(args: Array[String]): Unit = {

    //定义一个常量
    val name="刘能";
    //定义一个变量
    var id=1;

    //定义变量的时候会自动判断数据类型也可以自己制定
    var age : Int=30;
    //scala  数据类型都是对象 可以对基本数据类型调用方法
    val bool = age.equals(20)

    //调用方法 +()方法  相当于a+b
    println(age.+(20));

    println(bool)
    var address : String="上海";

    //打印
    println(name);

    println(id);

    println(age)

    println(address)


  }

}
