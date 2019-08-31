package com.itheima.hbase.observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;

import java.io.IOException;
import java.util.List;

/**
 * @program: hbase
 * @description: 1.自定义类继承BaseRegionObserver
 * 2.重写方法prePut  会在server中  执行put之前执行
 * 3.通过put获取rk 和其中的列值 创建新表 将数据封装到put
 * 4.相当于建立二级索引  查询的时候可以在新标准中查询某个值 获取rk  取表中获取详细信息
 * @author: Mr.Jiang
 * @create: 2019-08-16 11:32
 **/
public class RegionObserverDemo extends BaseRegionObserver {

    /**
     * @param e
     * @param put        拦截的put请求
     * @param edit
     * @param durability
     * @throws IOException
     */
    @Override
    public void prePut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        //获取rk
        byte[] rowkey = put.getRow();

        //获取列值为name的数据  cell
        List<Cell> cells = put.get("f1".getBytes(), "name".getBytes());
        Cell cell = cells.get(0);

        Configuration configuration = HBaseConfiguration.create();

        Connection connection = ConnectionFactory.createConnection(configuration);
        Admin admin = connection.getAdmin();

        //创建表构造器
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf("test11"));
        //创将列族构造器
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor("f1");

        hTableDescriptor.addFamily(hColumnDescriptor);

        //创建表
        admin.createTable(hTableDescriptor);

        Table table = connection.getTable(TableName.valueOf("test11"));

        //常见put对象 封装数据
        Put put1 = new Put(rowkey);
        put.add(cell);

        //关闭资源
        table.close();
        connection.close();


    }
}
