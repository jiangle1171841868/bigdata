package com.itheima.scalaclass

abstract class AbstractTest(val name: String) {

  //todo 抽象字段
  val id: Int

  //todo 抽象方法
  def getName
}

//todo继承
class Test(name: String) extends AbstractTest(name) {

  val id: Int = 1

  def getName: Unit = ???
}

