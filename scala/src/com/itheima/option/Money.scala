package com.itheima.option

class Money(val name: String) {}

object Money {

  def main(args: Array[String]): Unit = {

    //todo apply方法 就是使用构造器构造实例  接收参数 使用接收的参数 构造实例
    def apply(name: String) = {
      new Money(name)
    }

    // todo unapply方法 从对象中取出相应的值
    //todo Option  表示可能存在或者不存在的值  有两个子类Some和Any
    //todo  有值得时候 使用Some()获取值
    def unapply(money: Money): Option[String] = {

      if (money == null) {
        None
      }
      else {
        //todo name 需要是val var 没有写val 或者var 会没有那么属性
        Some(money.name)
      }
    }
  }
}

object OptionDemo {
  def main(args: Array[String]) {
    val map = Map("a" -> 1, "b" -> 2)
    val v = map.get("b") match {
      //todo 有值得时候使用Some()获取值
      case Some(a) => a
      case None => 0
    }
    println(v)
    //更好的方式
    val v1 = map.getOrElse("c", 0)
    println(v1)
  }
}
