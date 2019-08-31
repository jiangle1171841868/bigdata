package com.itheima.hbaseandes.utils;

import com.itheima.hbaseandes.bean.Article;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;


import java.io.IOException;
import java.util.List;


/**
 * @program: etl
 * @description: 1.获取表的方法  没有就创建 有就删除
 * 参数:表名 列族(可变参数)  返回值:Table
 * 2.将excle中的数据写入到hbase中
 * 参数:List<Article>  无返回值
 * 3.通过es中查询出的id 当做rk查询数据
 * 参数:表名 列族 列名 rk  返回值:查询的数据 string类型
 * @author: Mr.Jiang
 * @create: 2019-08-18 19:53
 **/
public class HbaseUtil {

    private static Connection connection = null;

    //静态代码块初始化client
    static {

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");
        //创建连接对象
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1.获取表的方法  没有就创建 有就删除
     * 参数:表名 列族(可变参数)  返回值:Table
     *
     * @param tableName
     * @param familys
     * @return
     */
    public static Table getTable(String tableName, String... familys) {

        TableName tablename = TableName.valueOf(tableName);
        Table table = null;
        //获取admin
        try {
            Admin admin = connection.getAdmin();
            //判断表是否存在
            if (!admin.tableExists(tablename)) {
                //不存在就创建

                //创建表构造器
                HTableDescriptor hTableDescriptor = new HTableDescriptor(tablename);
                //创建列族构造器
                for (String family : familys) {
                    HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(family);
                    hTableDescriptor.addFamily(hColumnDescriptor);
                }

                //创建表
                admin.createTable(hTableDescriptor);
                table = connection.getTable(tablename);
                admin.close();
            } else {

                //表存在就获取
                table = connection.getTable(tablename);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    /**
     * * 2.将excle中的数据写入到hbase中
     * * 参数:List<Article>  无返回值
     *
     * @param list
     */
    public static void putData(List<Article> list) {

        //获取table
        for (Article article : list) {
            putDataByRowKey("articles",  "title", article.getTitle(), article.getId(), "article");
            putDataByRowKey("articles",  "from", article.getFrom(), article.getId(), "article");
            putDataByRowKey("articles",  "time", article.getTime(), article.getId(), "article");
            putDataByRowKey("articles",  "readCount", article.getReadCount(), article.getId(), "article");
            putDataByRowKey("articles",  "content", article.getContent(), article.getId(), "article");
        }
    }

    /**
     * 插入数据的方法
     * @param tableName
     * @param columnName
     * @param columnValue
     * @param rowkey
     * @param familys
     */
    public static void putDataByRowKey(String tableName,  String columnName, String columnValue, String rowkey,String...familys) {

        //获取table对象
        Table table = getTable(tableName, familys);

        try {
            //创建put 对象
            Put put = new Put(rowkey.getBytes());

            for (String family : familys) {
                //插入数据
                put.addColumn(family.getBytes(), columnName.getBytes(), columnValue.getBytes());

                table.put(put);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关流
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据es查询得到的id  查询数据
     *
     * @param tableName
     * @param family
     * @param columnName
     * @param rowkey
     */
    public static String queryByRowKey(String tableName, String family, String columnName, String rowkey) {

        //获取table
        Table table = getTable(tableName, family);
        String content = "";

        try {
            //创建get对象
            Get get = new Get(rowkey.getBytes());
            get.addColumn(family.getBytes(), columnName.getBytes());
            Result result = table.get(get);
            //获取单元格
            byte[] value = result.getValue(family.getBytes(), columnName.getBytes());
            content = Bytes.toString(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

}
