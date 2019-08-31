package com.itheima.spark.iter

import org.apache.spark.rdd.{HadoopRDD, RDD}
import org.apache.spark.{SparkConf, SparkContext}

object WordCountIter {

  def main(args: Array[String]): Unit = {

    //todo 1.构建spark程序的入口SparkContext上下文实例对象
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("SparkWordCount")
      .setMaster("local[2]") //以本地模式运行

    //参数:config: SparkConf  需要SparkConf对象
    val sc: SparkContext = new SparkContext(sparkConf)

    //todo 2.读取文件 从项目中读取 文件放在项目统计路径  前面加./获取本地文件
    //todo 读取文件底层就是mr中的input
    /**
      * def textFile(
      * path: String,                                 todo //文件路径
      * minPartitions: Int = defaultMinPartitions)    todo //分区 有默认值可以不写 默认从hdfs中一个块就是一个分区 一个分区对应一个task
      * : RDD[String] =                               todo //方法返回值:抽象类RDD[String]
      * withScope {
      * assertNotStopped()                            todo //调用hadoopFile方法  作为方法的返回值
      * hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],minPartitions)   todo  //方法体最后一行方法的返回值
      * .map(pair => pair._2.toString)                todo //调用map方法对每一条数据进行处理  数据是k(偏移量)v(一行数据)  处理之后只要第二个数据
      * .setName(path)                                todo //设置名字
      *
      *
      * todo 参数列表:(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],minPartitions)
      * todo 文件路径 TextInputFormat对象(戳进去就是MR的TextInputFormat java类) 输出key类型 输出value类型  分区数
      *
      * todo hadoopFile方法
      * def hadoopFile[K, V](
      * path: String,                                        todo 文件路径
      * inputFormatClass: Class[_ <: InputFormat[K, V]],     todo InputFormat
      * keyClass: Class[K],                                  todo 输出key类型
      * valueClass: Class[V],                                todo 输出value类型
      * minPartitions: Int = defaultMinPartitions)           todo 分区数
      * : RDD[(K, V)] =
      * withScope {
      * assertNotStopped()
      *
      * // This is a hack to enforce loading hdfs-site.xml.
      * // See SPARK-11227 for details.
      *     FileSystem.getLocal(hadoopConfiguration)
      *
      * // A Hadoop configuration can be about 10 KB, which is pretty big, so broadcast it.
      * val confBroadcast = broadcast(new SerializableConfiguration(hadoopConfiguration))
      * val setInputPathsFunc = (jobConf: JobConf) => FileInputFormat.setInputPaths(jobConf, path)
      *
      * new HadoopRDD(   todo //最后一行返回值 HadoopRDD
      * this,
      * confBroadcast,
      * Some(setInputPathsFunc),
      * inputFormatClass,
      * keyClass,
      * valueClass,
      * minPartitions).setName(path)
      * }
      *
      *
      *
      */
    val inputRDD: RDD[String] = sc.textFile("./datas/wordcount/input/wordcount.data")

    /**
      * /**
      * *  Return a new RDD by first applying a function to all elements of this
      * *  RDD, and then flattening the results.
      **/
      * def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U] = withScope {
      * val cleanF = sc.clean(f)
      * new MapPartitionsRDD[U, T](this, (context, pid, iter) => iter.flatMap(cleanF))  todo 调用flatMap 返回是一个MapPartitionsRDD[U, T]
      * }
      */
    val wordCountRDD: RDD[String] = inputRDD.flatMap(line => line.split("\\s+").filter(line => line.length > 0))


    /**
      * /**
      * * Return a new RDD by applying a function to each partition of this RDD.
      * *
      * * `preservesPartitioning` indicates whether the input function preserves the partitioner, which
      * * should be `false` unless this is a pair RDD and the input function doesn't modify the keys.
      **/
      * def mapPartitions[U: ClassTag](
      * f: Iterator[T] => Iterator[U],
      * preservesPartitioning: Boolean = false): RDD[U] = withScope {
      * val cleanedF = sc.clean(f)
      *
      *
      * new MapPartitionsRDD(  //todo 调用mapPartitions函数 返回值是MapPartitionsRDD
      * this,
      * (context: TaskContext, index: Int, iter: Iterator[T]) => cleanedF(iter),
      * preservesPartitioning)
      * }
      */
    //todo 使用{}比较方便
    //todo   def compute(split: Partition, context: TaskContext): Iterator[T]  并行计算有个上下文对象
    //todo def getPartitionId(): Int  可以获取分区id等方法
    val wordsCountRDD: RDD[(String, Int)] = wordCountRDD.mapPartitions { iter =>
      //todo 不清楚数据类型 就获取一下
      //val xx: Iterator[String] = iter
      iter.map(word => (word, 1))
    }

      /**
        * todo  往里面戳 最后是返回值ShuffledRDD
        */
      .reduceByKey { (x, y) => x + y }



    //todo 排序  sortByKey()是OrderedRDDFunctions的函数 为什么RDD能用???
    //todo 因为RDD的伴生对象定义了隐式转换 使RDD能够调用OrderedRDDFunctions的方法
    //todo  implicit def rddToOrderedRDDFunctions[K : Ordering : ClassTag, V: ClassTag](rdd: RDD[(K, V)])
    //    : OrderedRDDFunctions[K, V, (K, V)] = {
    //    new OrderedRDDFunctions[K, V, (K, V)](rdd)   返回值是OrderedRDDFunctions
    //  }
    wordsCountRDD.sortByKey()

    //todo 底层调用的是sortByKey()
    wordsCountRDD.sortBy(_._2)

    wordsCountRDD.foreachPartition { iter =>
      iter.foreach { case (k, v) => println(s"$k->$v") }

    }
  }

}
