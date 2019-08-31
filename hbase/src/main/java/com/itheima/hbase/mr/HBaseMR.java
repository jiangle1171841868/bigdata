package com.itheima.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.mapreduce.Job;


import java.io.IOException;

/**
 * @program: hbase
 * @description: 将hbase的表test的数据导入到hbase的表test2中
 * 创建一个map继承TableMapper     将rowkey取出 封装到k2 将数据封装到put对象中  往下输出
 * 创建一个reduce继承TableReduce  直接输出
 * @author: Mr.Jiang
 * @create: 2019-08-15 12:02
 **/
public class HBaseMR  {

    public static void main(String[] args) throws Exception {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");

        Job job = Job.getInstance(configuration, HBaseMR.class.getName());
        //jar包运行入口
        job.setJarByClass(HBaseMR.class);

        //创建scan对象
        Scan scan = new Scan();
        scan.setCaching(500);
        scan.setCacheBlocks(false);

        //组装job
        //使用工具类组装tablemap
        /**
         * 参数: String table,
         *       Scan scan,
         *       Class<? extends TableMapper> mapper,
         *       Class<?> outputKeyClass,
         *       Class<?> outputValueClass,
         *       Job job
         */
        TableMapReduceUtil.initTableMapperJob(
                "test", //输入的表名
                scan,
                HBaseMrMap.class,
                ImmutableBytesWritable.class,//输出的k2
                Put.class,                   //输出的v2
                job
        );

        //组装输出类型
        /**
         * 参数:String table,
         *     Class<? extends TableReducer> reducer,
         *     Job job
         */
        TableMapReduceUtil.initTableReducerJob(
                "test3",
                null, //不需要reduce
                job
        );

        job.setNumReduceTasks(0);

        job.waitForCompletion(true);

    }




    //<ImmutableBytesWritable, Result, KEYOUT, VALUEOUT>
    //static修饰 静态的内部类
    public static class HBaseMrMap extends TableMapper<ImmutableBytesWritable, Put> {

        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {

            context.write(key, resultToPut(key, value));
        }

        private static Put resultToPut(ImmutableBytesWritable key, Result value) throws IOException {

            Put put = new Put(key.get());
            //根据rk获取每一行的内容
            Cell[] cells = value.rawCells();
            for (Cell cell : cells) {
                put.add(cell);
            }
            return put;
        }
    }

}
