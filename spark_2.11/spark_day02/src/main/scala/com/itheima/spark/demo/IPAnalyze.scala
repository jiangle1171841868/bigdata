package com.itheima.spark.demo

import java.sql.{Connection, Driver, DriverManager, PreparedStatement}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/// TODO: 统计 每个经纬度的ip数  写入到mysql
object IPAnalyze {

  def main(args: Array[String]): Unit = {

    //todo 1.构建SparkContext对象
    val sc: SparkContext = {
      val sparkConf: SparkConf = new SparkConf()
        .setAppName("SparkWordCount")
        .setMaster("local[5]")
      SparkContext.getOrCreate(sparkConf)
    }
    //设置日志级别
    sc.setLogLevel("WARN")

    //todo 2.读取ip地址库文件  获取起始ip  结束ip(long类型的数据) 以及经纬度信息 四个字段组成四元组  或者 封装到bean中   最后转化为数组ipInfoArray
    val ipRDD: RDD[String] = sc.textFile("datas/ips/ip.txt", 5)

    //1.过滤
    val ipInfoArray: Array[(Long, Long, String, String)] = ipRDD.filter(ipInfo => ipInfo != null && ipInfo.trim.split("\\|").length >= 15)

      //解析数据成四元组
      .mapPartitions { iter =>
      iter.map { ipInfo =>
        val arr: Array[String] = ipInfo.trim.split("\\|")
        (arr(2).toLong, arr(3).toLong, arr(13), arr(14))
      }
    }
      //将RDD转化为数组 (适用于数据量不大时)
      .collect()

    //todo  将数组广播到每个executor中  使用的时候需要ipInfoArrayBroadcast.value
    val ipInfoArrayBroadcast: Broadcast[Array[(Long, Long, String, String)]] = sc.broadcast(ipInfoArray)

    //todo 3.读取日志数据      获取ip   转化为long类型(封装方法) iplong
    val ipInfoRDD: RDD[String] = sc.textFile("datas/ips/20090121000132.394251.http.format")

    //获取一条数据的ip 取比对是哪个经纬度的ip  然后经纬度计数1返回  操作的都是一条数据
    val info: RDD[((String, String), Int)] = ipInfoRDD
      .filter { iter => iter != null && iter.trim.split("\\|").length >= 2 }
      .mapPartitions { line =>
        line.map { ipInfo =>
          val array: Array[String] = ipInfo.trim.split("\\|")
          val ip = array(1)
          val ipLong: Long = ipToLong(ip)
          //调用方法 获取ip所在范围的索引 直接返回索引
          val index: Int = binarySearch(ipLong, ipInfoArrayBroadcast.value)
          index
        }
          //过滤出index为-1的数据 去掉
          .filter(index => index != -1)
          //根据索引获得经纬度  将经纬度作为一个key  计数为1
          .map { index =>
          val (_, _, longitude, latitude) = ipInfoArrayBroadcast.value(index)
          //返回值
          ((longitude, latitude), 1)
        }
      }
      .reduceByKey(_ + _)

    info.foreach(println)
    //将数据保存到数据库
    //降低分区数目
    info
      .coalesce(1)
      .foreachPartition { iter =>
        saveToMysql(iter)
      }
    Thread.sleep(10000000)

    // 应用结束，关闭资源
    sc.stop()
  }


  /**
    * 封装方法  判断ip在哪个ip分组里面返回索引
    *
    * @param ipLong
    * @param ipInfoArray
    * @return 返回索引 没有数据就返回-1
    */
  def binarySearch(ipLong: Long, ipInfoArray: Array[(Long, Long, String, String)]): Int = {

    //val 定义起始索引和结束索引
    var startIndex = 0
    var endIndex = ipInfoArray.length - 1

    //二分法 循环运行的条件
    while (startIndex <= endIndex) {

      //获取中间值  通过判断条件修改startIndex 和 endIndex动态变化
      val middleIndex: Int = startIndex + (endIndex - startIndex) / 2

      //根据中间值获取ip范围进行匹配
      val (startIP, endIP, _, _) = ipInfoArray(middleIndex)
      //todo 判断1:如果ipLong>=startIP 和  ipLong<=endIP时 就是在范围之内直接返回索引
      if (ipLong >= startIP && ipLong <= endIP) {
        return middleIndex
        //当ipLong < startIP 往数组左边查找
      } else if (ipLong < startIP) {
        endIndex = middleIndex - 1
        //当ipLong > endIP 往数组右边查找
      } else {
        startIndex = middleIndex + 1
      }

    }
    //没有返回值 返回-1
    -1
  }

  /**
    * 将IPv4格式值转换为Long类型值
    *
    * @param ip
    * IPv4格式的值
    * @return
    */
  def ipToLong(ip: String): Long = {
    val fragments: Array[String] = ip.split("\\.")
    var ipNum = 0L
    for (i <- 0 until fragments.length) {
      ipNum = fragments(i).toLong | ipNum << 8L
    }
    // 返回
    ipNum
  }

  def saveToMysql(iter: Iterator[((String, String), Int)]): Unit = {

    var connection: Connection = null
    var pstmt: PreparedStatement = null

    //todo 1.加载驱动
    try {
      Class.forName("com.mysql.jdbc.Driver")

      //todo 获取连接
      connection = DriverManager.getConnection("jdbc:mysql://node03:3306/test", "root", "123456")

      //todo sql语句  设置联合主键经度和纬度  经纬度一样就修改
      val sqlStr: String = "INSERT INTO tb_iplocation (longitude, latitude, total_count) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE total_count=VALUES(total_count)"

      //todo 创建PreparedStatement对象
      pstmt = connection.prepareStatement(sqlStr)

      // 将迭代器中数据插入到表中
      iter.foreach { case ((longitude, latitude), count) =>
        println(s"经度 = $longitude, 维度 = $latitude, 次数 = $count")
        pstmt.setString(1, longitude)
        pstmt.setString(2, latitude)
        pstmt.setLong(3, count)

        // 将其加入批次中
        pstmt.addBatch()
      }
      // 执行批量插入
      pstmt.executeBatch()
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      //关闭资源
      if (pstmt != null) {
        pstmt.close()
      }
      if (connection != null) {
        connection.close()
      }

    }


  }
}
