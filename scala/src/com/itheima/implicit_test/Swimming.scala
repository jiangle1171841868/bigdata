package com.itheima.implicit_test

//todo 定义隐式方法 使没有swiming方法的猫也可以调用swiming方法
class Swimming {

  def swimming = println("游泳技能...")

}

class Cat {}

//todo 定义隐式方法在object类型 可以不用new直接调用 所有的隐式值隐式方法 必须放在object里面
object ImplicitTest {

  //todo 定义隐式方法 需要implicit关键字
  // 参数列表:需要调用别人方法的对象
  // 方法体:new 拥有方法的对象

  implicit def getSwimming(cat: Cat) = new Swimming

}

object Cat {

  def main(args: Array[String]): Unit = {

    val cat = new Cat

    //todo 注意:需要先导包 不然还是调用不了  是在哪个包object下定义的隐式方法就导入哪个包
    import com.itheima.implicit_test.ImplicitTest._
    cat.swimming
  }
}