package com.itheima.trait_test


//todo  继承第一个trait使用extends   第二个之后 使用 with
//todo 继承之后要重写私有属性和方法  使用Ctrl+I
class TrailExtends extends TraitTest01 with TraitTest02 {

  override val name: String = "lisi"

  override def sayHello: Unit = println()

  override def sayByte: Unit = println()

}

//todo 继承实体方法 可以直接调用
class TrailExtends2 extends TraitTest03 {
  /*override val name: String = "张三"

  override def sayHello: Unit = println("hello")*/
}

object TrailExtends2{
  def main(args: Array[String]): Unit = {

    //todo 创建TraitExtends2的实例
    val t1 = new TrailExtends2

    //todo 调用TraitTest03的实体方法
    t1.sayHaHa

    //todo 实例对象 混入哪个trait  就可以使用哪个trait的方法
    //todo 创建TraitExtends2的实例  在实例对象中混入TraitTest01 就可以使用TraitTest01 的方法
    val t2 = new TrailExtends2 with TraitTest01 {
      override val name: String = "hello TraitTest01"

      override def sayHello: Unit = println("hello TraitTest01")
    }
    //调用方法
    t2.sayHello


    //todo 创建TraitExtends2的实例 在实例对象中混入TraitTest02 就可以使用TraitTest02 的方法
    val t3 = new TrailExtends2 with TraitTest02 {
      override def sayByte: Unit = println("hello TraitTest02")
    }
    //调用方法
    t3.sayByte

  }

}