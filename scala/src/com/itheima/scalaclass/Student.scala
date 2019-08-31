package com.itheima.scalaclass

//todo  主构造器的参数直接放置在类名后面
//todo  主构造器 会执行类中的所有语句  除了属性 方法之外 所有的都是主构造器的内容
//todo  构造器中的字段  会自动提升成为类的中的属性 因此不需要在class中重新定义  也定义不了
class Student(val id: Int, val name: String) {

  println("主构造器执行开始")

  private var age = 20


  def sayHello = println("hello scala!")

  println("主构造器执行结束")


  //todo 辅助构造器
  def this(id: Int, name: String, age: Int) {

    //todo 每个辅助构造器第一行 必须调用主构造器 或者 其他构造器  将new对象传入的参数     传入到主构造器中
    this(id, name)
    this.age = age

    println("执行辅助构造器")

  }


}

//todo 使用构造器创建对象
object Student {

  def main(args: Array[String]) {

    //主构造器
    val s1 = new Student(1, "刘能") // 打印信息:执行主构造器

    //辅助构造器
    val s2 = new Student(1, "刘能", 20)
    //先调用主构造器 执行除了属性和方法的所有内容
    //构建辅助构造器 执行除了属性和方法的所有内容
    //构造完成之后  再调用方法

    //调用方法
    s2.sayHello
    /*    主构造器执行开始
        主构造器执行结束
        主构造器执行开始
        主构造器执行结束
        执行辅助构造器
        hello scala!*/
  }
}

//todo 继承有构造器的类 需要传入主构造器需要的参数 不需要指定val  或者 var 指定就认为要覆盖父类
class Student1(id: Int, name: String) extends Student(id: Int, name: String)
