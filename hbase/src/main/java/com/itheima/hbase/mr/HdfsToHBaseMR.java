package com.itheima.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TestCellComparator;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableMap;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.hsqldb.Table;

import java.io.IOException;
import java.util.Map;

/**
 * @program: hbase
 * @description: hdfs将数据导入到hbase
 * @author: Mr.Jiang
 * @create: 2019-08-15 19:06
 **/
public class HdfsToHBaseMR extends Configured implements Tool {


    public static void main(String[] args) throws Exception {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");

        int run = ToolRunner.run(configuration, new HdfsToHBaseMR(), args);

        System.exit(run);
    }

    @Override
    public int run(String[] strings) throws Exception {

        //创建job
        Job job = Job.getInstance(this.getConf(), HdfsToHBaseMR.class.getName());

        //jar入口
        job.setJarByClass(HdfsToHBaseMR.class);

        //input
        Path inputPath = new Path("hdfs://node01:8020/hbase/input/a");
        FileInputFormat.setInputPaths(job, inputPath);

        //组装map
        job.setMapperClass(HdfsToHBaseMap.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        //shuffle

        //初始化reducer 将数据写到hbase中 使用TableMapReduceUtils组装
        //String table,
        //  Class<? extends TableReducer> reducer,
        //  Job job
        TableMapReduceUtil.initTableReducerJob(
                "test4",
                HdfsToHBaseReducer.class,
                job
        );

        //提交
        return job.waitForCompletion(true) ? 1 : 0;

    }

    /**
     * 直接将数据输出 在reducer中封装成put对象
     */
    public static class HdfsToHBaseMap extends Mapper<LongWritable, Text, Text, NullWritable> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //直接输出
            context.write(value, NullWritable.get());
        }
    }

    public static class HdfsToHBaseReducer extends TableReducer<Text, NullWritable, ImmutableBytesWritable> {

        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

            String[] split = key.toString().split(",");
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
