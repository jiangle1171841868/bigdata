package com.itheima.scalatest

object IfTest {

  //Unit 没有返回值
  def main(args: Array[String]): Unit = {

    var a: Int = 1;

    //判断a的值  赋值给b
    var b = if (a > 0) 1 else 0;
    println(b);

    //支持混合类型的值
    var c = if (a > 2) 1 else "判断错了...hello scala";
    println(c);

    //没有else  相当于 if (a > 2)  1 else ()  判断条件为else时 会输出()
    var d = if (a > 2) 1;
    println(d);

    //if 和 else if
    var f = if (a > 2) 1
    else if (a > 0) 2
    else "hi"

    println(f)



  }
}
