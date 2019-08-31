package com.itheima.scalaclass

//todo 类没有静态方法 不能通过类名直接调用方法和属性
//todo object里面的属性和方法都是静态的

//todo 可以将对象的创建在object中
// 1.new Person 赋值给属性
// 2.或者 创建方法 方法的返回值是对象
class Person {

  //todo val修饰的属性不可以修改  只有getter 没有setter
  val name: String = "张三"

  //todo var修饰的变量可以修改 有getter和setter
  var age: Int = 20

  //todo private修饰的变量只有   自己内部(包括内部类) 和 伴生对象  可见
  private var id = 1

  //todo 使用[this]  伴生对象也不能访问
  private[this] var pet = "1"


  class People(){

    //todo 内部类也在内部 所有的都可以访问
  }

}

//todo 一个类型可以有很多个objec对象 里面的属性的方法都是静态的
//todo 伴生对象 需要符合两个条件
//todo 1.类和object在同一个 .scala文件中 2.类名和object名相同
object Person {

  //todo   new Person 赋值给 属性
  val person = new Person

  def main(args: Array[String]) {

    //todo 创建一个对象
    val p = new Person
    //todo 访问类的私有变量
    p.id = 2
    println(p.id)
  }

  //todo   创建一个方法 返回值是 对象
  def getPerson(): Person = person


}

object Person1 {

  def main(args: Array[String]) {
    //todo 创建一个对象
    val p = new Person
    //不是伴生对象访问不了私有属性  只能访问非私有属性
    p.age = 30

  }

}