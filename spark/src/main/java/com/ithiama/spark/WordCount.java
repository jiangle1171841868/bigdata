package com.ithiama.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;


import java.util.Arrays;

/**
 * @program: spark
 * @author: Mr.Jiang
 * @create: 2019-08-24 14:03
 * @description:
 **/
public class WordCount {

    public static void main(String[] args) {

        //todo 1.构建spark程序application的上下文对象
        SparkConf config = new SparkConf().setAppName("wordCount").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(config);

        //todo 2.读取文件
        JavaRDD<String> inputRDD = sc.textFile("/datas/sparkInput/wc.txt");

        //todo 3.切割压缩
        JavaPairRDD<String, Integer> wordCountsRDD = inputRDD.flatMap(line -> Arrays.asList(line.trim().split(" ")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey(((v1, v2) -> v1 + v2));

        wordCountsRDD.foreach(tuple-> System.out.println("tuple = " + tuple.toString()));

        sc.stop();
    }
}
