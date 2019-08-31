package com.itheima.hbase.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;

import java.io.IOException;

/**
 * @program: hbase
 * @description: 将hdfs中的HFile格式的数据加载到数据表中
 * @author: Mr.Jiang
 * @create: 2019-08-15 19:59
 **/
public class LoadData {

    public static void main(String[] args) throws Exception {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");
        Connection connection = ConnectionFactory.createConnection(configuration);
        Admin admin = connection.getAdmin();
        Table table = connection.getTable(TableName.valueOf("test5"));
        //获取region的坐标
        RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("test5"));

        //创建加载对象  参数Configuration
        LoadIncrementalHFiles loadIncrementalHFiles = new LoadIncrementalHFiles(configuration);

        //加载HFile数据
        //Path hfofDir,
        // final Admin admin,
        // Table table,
        //RegionLocator regionLocator
        loadIncrementalHFiles.doBulkLoad(new Path("hdfs://node01:8020/hbase/out"),admin,table,regionLocator);

        //关流
        table.close();
        admin.close();
        connection.close();
    }
}
