package com.itheima.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * @program: hbase
 * @description: 通过bulkload的方式批量加载数据到hbase中
 * 1.先以HFile格式将数据存储到hdfs中
 * 2.加载数据到hbase表中
 * @author: Mr.Jiang
 * @create: 2019-08-15 19:45
 **/
public class BulkloadToHBase  {

    public static void main(String[] args) throws Exception {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");

        int run = ToolRunner.run(configuration, new HdfsToHBaseMR(), args);

        Connection connection = ConnectionFactory.createConnection(configuration);
        Table table = connection.getTable(TableName.valueOf("test5"));
        RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("test5"));
        //创建job
        Job job = Job.getInstance(configuration, HdfsToHBaseMR.class.getName());

        //jar入口
        job.setJarByClass(BulkloadToHBase.class);

        //input
        Path inputPath = new Path("hdfs://node01:8020/hbase/input/a");
        FileInputFormat.setInputPaths(job, inputPath);

        //组装map
        job.setMapperClass(HdfsToHBaseMap.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);
        //output 输出文件类型为HFileOutputFormat2
        //创建HFileOutputFormat2对象
        //Job job,
        // Table table,
        // RegionLocator regionLocator
        HFileOutputFormat2.configureIncrementalLoad(job, table, regionLocator);
        job.setOutputFormatClass(HFileOutputFormat2.class);
        FileOutputFormat.setOutputPath(job, new Path("hdfs://node01:8020/hbase/out4/"));
        table.close();
        connection.close();
        //提交
       job.waitForCompletion(true);


    }

    /**
     * 将数据转化为put对象输出
     */
    public static class HdfsToHBaseMap extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String[] split = value.toString().split(",");
            System.out.println(split.length);
            //获取rk
            String rowkey = split[0];

            String name = split[1];

            String age = split[2];

            //创建put对象 指定rk
            Put put = new Put(rowkey.getBytes());

            //添加列
            put.addColumn("f1".getBytes(), "name".getBytes(), name.getBytes());
            put.addColumn("f1".getBytes(), "age".getBytes(), age.getBytes());

            //输出 使用ImmutableBytesWritable(rowkey.getBytes()) 输出主键
            context.write(new ImmutableBytesWritable(rowkey.getBytes()), put);
        }
    }


}
