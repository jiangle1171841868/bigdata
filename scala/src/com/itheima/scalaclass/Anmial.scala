package com.itheima.scalaclass

class Anmial {

  //todo  val修饰的属性才能被继承
  val name: String = "zhangsan"

  //todo var修饰的不能被继承
  var age: Int = 20

  def m1(x: Int, y: Int) = {
    println(x + "+" + y);
    x + y
  }

  def getName = this.name

}

class Dog extends Anmial {

  //todo 覆盖父类的方法和属性之后可以修改
  //todo 覆盖父类的val修饰属性 要加上override 关键字 因为不可变
  //todo 修改父类var修饰的属性  可以直接修改  因为可变
  override val name: String = "lisi"

  //todo  覆盖父类的非抽象方法 要加上override 关键字
  override def m1(x: Int, y: Int): Int = x - y + super.m1(x, y)

  //todo 子类覆盖父类方法之后  调用父类方法 需要使用super关键字
  override def getName: String = "hello scala!" + super.getName

}


class Cat extends Anmial {

  //获取父类属性
  println(name)

  //调用父类方法
  m1(1, 2)

  //直接修改父类var修饰的属性
  age = 30
}


