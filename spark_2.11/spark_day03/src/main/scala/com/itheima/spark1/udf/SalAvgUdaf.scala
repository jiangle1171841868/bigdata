package com.itheima.spark1.udf

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, IntegerType, StringType, StructField, StructType}


/// TODO: 自定义一个udaf 聚合函数 求各个部门工资的平均工资
object SalAvgUdaf extends UserDefinedAggregateFunction {

  /// TODO: 输入数据类型 可以多个
  override def inputSchema: StructType = {
    StructType(
      Array(
        StructField("salary", StringType, true)
      )
    )
  }

  /// TODO: 临时变量数据类型 可以多个
  // 只要修改数据  就使用buffer调用update方法 更新数据   通过索引来确定更新那个临时变量
  // 初始化临时变量的值 分区聚合 全局聚合 都需调用update方法更新
  override def bufferSchema: StructType = {
    StructType(
      Array(
        StructField("salary_count", IntegerType, true), //更新数据时  索引为0  buffer.update(0, 0)
        StructField("salary_total", DoubleType, true) //更新数据时  索引为1  buffer.update(1, 0.0)
      )
    )
  }

  /// TODO: 输出数据类型  直接等于数据类型就可以
  override def dataType: DataType = DoubleType

  /// TODO:函数唯一性
  override def deterministic: Boolean = ???

  /// TODO: 临时变量初始值
  /**
    * abstract class MutableAggregationBuffer extends Row {
    *
    * /** Update the ith value of this buffer. */
    * 底层调用的是update方法
    * def update(
    * i: Int,       - 临时变量的索引
    * value: Any    - 更新的值
    * ): Unit
    * }
    */

  override def initialize(buffer: MutableAggregationBuffer): Unit = {

    buffer.update(0, 0)
    buffer.update(1, 0.0)
  }

  /// TODO: 分区聚合
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    //读取的每一条数据封装在row中  通过row取出每一个工资进行聚合
    val salary = input.getAs[Double]("salary")

    //获取临时变量的值
    val salary_count = buffer.getAs[Integer]("salary_count")
    val salary_total = buffer.getAs[Double]("salary_total")

    //调用update方法更新数据
    buffer.update(0, salary_count + 1)
    buffer.update(1, salary_total + salary)

  }

  /// TODO: 全局聚合
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {

    // 分别获取中间临时变量的值
    val salary_count1 = buffer1.getAs[Integer]("salary_count")
    val salary_total1 = buffer1.getAs[Double]("salary_total")

    val salary_count2 = buffer2.getAs[Integer]("salary_count")
    val salary_total2 = buffer2.getAs[Double]("salary_total")

    // 合并及更新
    buffer1.update(0, salary_count1 + salary_count2)
    buffer1.update(1, salary_total1 + salary_total2)
  }

  /// TODO: 最终结果
  override def evaluate(buffer: Row): Any = {

    //获取聚合结果
    buffer.getAs[Double]("salary_total") / buffer.getAs[Integer]("salary_count")


  }
}
