package com.itheima.case_class

import scala.util.Random

//todo case class  :多例模式 后面必须跟构造器 可以没有参数  没有构造器就报错  创建实例的时候不用new  伴生对象会帮我们实现apply方法
//todo case object :单例 不能跟构造器  加了就报错  object静态的 不用new
case class CaseClassTest()

case class CaseClassTest2(name: String)

case object CaseClassTest3

//todo 样例器匹配
object Test extends App{

  // 创建一个数组 里面是样例类对象
  val arr = Array(CaseClassTest(), CaseClassTest2("刘能"), CaseClassTest3,2)

  //产生一个随机数  随机获取数组元素  样例对象
  private val value: Any = arr(Random.nextInt(arr.length))

  //匹配
  value match {
    case CaseClassTest() => println(s"CaseClassTest")
    case CaseClassTest2(name) => println(s"CaseClassTest2,$name")
    case CaseClassTest3 => println(s"CaseClassTest3")
      //匹配其他
    case _ =>println("匹配不到啊...")
  }

}