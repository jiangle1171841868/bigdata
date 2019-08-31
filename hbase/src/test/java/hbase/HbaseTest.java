package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: hbase
 * @description:
 * @author: Mr.Jiang
 * @create: 2019-08-14 14:38
 **/
public class HbaseTest {


    private Connection connection;
    private Admin admin;
    private TableName tableName;
    private Table table;

    @Test
    public void createTable() throws IOException {

        //1.创建配置对象
        /**
         * 创建对象:
         * 1.找构造方法
         * 2.找方法返回值是对象本身的 带菱形的方法是静态方法 可以直接调用
         */
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");

        //创建连接
        Connection connection = ConnectionFactory.createConnection(configuration);

        //创建集群的客户端实例
        Admin admin = connection.getAdmin();

        //构建表描述器
        TableName tableName = TableName.valueOf("demo");
        HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);

        //判断表是否存在
        if (!admin.tableExists(tableName)) {
            //构建列族描述器
            HColumnDescriptor info = new HColumnDescriptor("info");
            HColumnDescriptor data = new HColumnDescriptor("data");
            hTableDescriptor.addFamily(info);
            hTableDescriptor.addFamily(data);

            //创建表  创建表的时候可以指定预分区
            admin.createTable(hTableDescriptor);
            admin.close();
            connection.close();
        }
    }

    /**
     * 初始化方法 表不存在就创建 存在就获取
     */
    @Before
    public void init() throws IOException {

        //创建配置对象
        Configuration configuration = HBaseConfiguration.create();
        //zookeeper
        configuration.set("hbase.zookeeper.quorum", "node01:2181,node02:2181,node03:2181");

        //创建连接   需要HBaseConfiguration配置信息对象  创建方式HBaseConfiguration.create()
        connection = ConnectionFactory.createConnection(configuration);

        //创建客户端操作对象
        Admin admin = connection.getAdmin();

        //创建表构造器   需要参数TableName对象 创建方式 里面有个静态方法 ValueOf()  返回值为TableName
        //import org.apache.hadoop.hbase.TableName;  不要导错包
        //导错包问题:没有想要的方法
        tableName = TableName.valueOf("test");

        //判断表是否存在
        if (!admin.tableExists(tableName)) {

            //不存在就创建
            //构造方法
            //创建对象的方法 方法返回值为对象本身
            HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);

            //添加列族  参数HColumnDescriptor对象
            tableDescriptor.addFamily(new HColumnDescriptor("f1"));
            tableDescriptor.addFamily(new HColumnDescriptor("f2"));

            //创建表  需要HTableDescriptor对象
            admin.createTable(tableDescriptor);
            table = connection.getTable(tableName);

        } else {

            //存在就获取表  参数TableName对象
            table = connection.getTable(tableName);
        }
    }

    @After
    public void clear() {

        //关闭资源  要进行非空校验 如果不存在就关闭 会空指针异常

        if (table != null) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (admin != null) {
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 批量插入数据
     */
    @Test
    public void insertDate() throws IOException {

        //创建put对象 参数:byte [] row  之间rk
        Put put = new Put("0001".getBytes());
        //插入列 和 值
        //参数:列族,列名.列值
        put.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(0));
        put.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("大乔"));
        put.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(20));
        put.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("江东"));
        put.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("16888888888"));
        put.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("helloworld"));

        Put put1 = new Put("0002".getBytes());
        put1.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(1));
        put1.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("曹操"));
        put1.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(30));
        put1.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put1.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("沛国谯县"));
        put1.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("16888888888"));
        put1.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("helloworld"));

        Put put2 = new Put("0003".getBytes());
        put2.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(2));
        put2.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("刘备"));
        put2.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(32));
        put2.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put2.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("幽州涿郡涿县"));
        put2.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("17888888888"));
        put2.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("talk is cheap , show me the code"));


        Put put3 = new Put("0004".getBytes());
        put3.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(3));
        put3.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("孙权"));
        put3.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(35));
        put3.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put3.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("下邳"));
        put3.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("12888888888"));
        put3.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("what are you 弄啥嘞！"));

        Put put4 = new Put("0005".getBytes());
        put4.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(4));
        put4.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("诸葛亮"));
        put4.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(28));
        put4.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put4.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("四川隆中"));
        put4.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("14888888888"));
        put4.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("出师表你背了嘛"));

        Put put5 = new Put("0006".getBytes());
        put5.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(5));
        put5.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("司马懿"));
        put5.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(27));
        put5.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put5.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("哪里人有待考究"));
        put5.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("15888888888"));
        put5.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("跟诸葛亮死掐"));


        Put put6 = new Put("0007".getBytes());
        put6.addColumn("f1".getBytes(), "id".getBytes(), Bytes.toBytes(5));
        put6.addColumn("f1".getBytes(), "name".getBytes(), Bytes.toBytes("xiaobubu—吕布"));
        put6.addColumn("f1".getBytes(), "age".getBytes(), Bytes.toBytes(28));
        put6.addColumn("f2".getBytes(), "sex".getBytes(), Bytes.toBytes("1"));
        put6.addColumn("f2".getBytes(), "address".getBytes(), Bytes.toBytes("内蒙人"));
        put6.addColumn("f2".getBytes(), "phone".getBytes(), Bytes.toBytes("15788888888"));
        put6.addColumn("f2".getBytes(), "say".getBytes(), Bytes.toBytes("貂蝉去哪了"));

        //将数据封装到集合
        List<Put> puts = new ArrayList<>();
        puts.add(put);
        puts.add(put);
        puts.add(put1);
        puts.add(put2);
        puts.add(put3);
        puts.add(put4);
        puts.add(put5);
        puts.add(put6);

        //插入数据 参数:Put  或者List<Put>
        table.put(puts);
    }

    /**
     * get查询数据  需要指定rowkey 没有rowkey查询不到数据
     */
    @Test
    public void queryByRowKey() throws IOException {

        Get get = new Get("0001".getBytes());
        //通过rk获取数据 参数: Get  List<Get>
        //获取的是一行数据
        Result result = table.get(get);

        //通过列族 列名 获取数据 参数:byte [] family, byte [] qualifier
        //获取的是hbase中存储的二进制文件
        byte[] name = result.getValue("f1".getBytes(), "name".getBytes());

        //System.out.println("name = " + name);
        //转化为数据对应的数据类型string输出 不一定是string 根据需要判断
        //两种方式转化
        System.out.println(new String(name));
        System.out.println(Bytes.toString(name));
    }

    @Test
    public void queryByRowKey2() throws IOException {

        Get get = new Get("0001".getBytes());
        Result result = table.get(get);
        //解析输出
        parseCell(result);
    }

    /**
     * get  使用数组 获取多个rk的值
     *
     * @throws IOException
     */
    @Test
    public void queryByRowKey3() throws IOException {

        Get get1 = new Get("0001".getBytes());
        Get get2 = new Get("0002".getBytes());
        Get get3 = new Get("0003".getBytes());
        Get get4 = new Get("0004".getBytes());

        List<Get> gets = new ArrayList<>();
        gets.add(get1);
        gets.add(get2);
        gets.add(get3);
        gets.add(get4);
        Result[] result = table.get(gets);

        for (Result result1 : result) {
            //解析输出
            parseCell(result1);
        }
    }

    @Test
    public void queryByRowKey4() throws Throwable {


       /* Scan scan = new Scan();
        AggregationClient aggregationClient = new AggregationClient(configuration);
        //final TableName tableName, final ColumnInterpreter<R, S, P, Q, T> ci, final Scan scan)
        System.out.println("RowCount: " + aggregationClient.rowCount(tableName, new LongColumnInterpreter(), scan));*/

    }

    /**
     * 通过scan查询 startrow endrow
     */
    @Test
    public void queryByRowKeyRang() throws IOException {

        //包括开始 不包括结束 查询的结果为0001  0002
        //在shell中叫做end  在javaapi中叫做stoprow
        String startRow = "0001";
        String endRow = "0003";

        /**
         * 查询:
         * 第一种方式:构造函数  开始 结束 rk
         * 第二种方式:创建scan对象 setStartRow setStopRow方法设置 开始结束
         */
        //Scan scan = new Scan(startRow.getBytes(),endRow.getBytes());
        Scan scan = new Scan();
        scan.setStartRow(startRow.getBytes());
        scan.setStopRow(endRow.getBytes());
        //scan查询  参数:scan对象
        ResultScanner results = table.getScanner(scan);
        for (Result result : results) {
            parseCell(result);
        }
    }

    /*
     * row过滤器
     * 过滤出大于 小于 等于 不等于rk的数据  里面是一个枚举
     *  public enum CompareOp {
     *     *//** less than *//*
     *           LESS, 小于
     *     *//** less than or equal to *//*
     *LESS_OR_EQUAL,  小于等于
     *     *//** equals *//*
     *EQUAL, 等于
     *     *//** not equal *//*
     *NOT_EQUAL, 不等于
     *     *//** greater than or equal to *//*
     *GREATER_OR_EQUAL,
     *     *//** greater than *//*
     *GREATER,
     *     */

    /**
     * no operation
     *//*
            *NO_OP,
            *
}*/
    @Test
    public void rowFilter() throws IOException {

        //创建row过滤器  导包要注意
        //参数   CompareOp rowCompareOp,
        //      final ByteArrayComparable rowComparator) 是一个抽象类  使用子类BinaryComparator

        //        //查询 row小于0003的数据
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.LESS, new BinaryComparator("0003".getBytes()));
        Scan scan = new Scan();
        scan.setFilter(rowFilter);

        ResultScanner results = table.getScanner(scan);
        parseResults(results);
    }

    /**
     * 列族过滤器
     * 获取列族等于 f1 的数据
     *
     * @throws IOException
     */
    @Test
    public void familyFilter() throws IOException {

        //创建列族过滤器对象
        //参数:final CompareOp familyCompareOp,
        //    final ByteArrayComparable familyComparator  是一个抽象类  使用子类SubstringComparator 模糊匹配
        FamilyFilter familyFilter = new FamilyFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator("f1"));
        //创建scan对象
        Scan scan = new Scan();
        scan.setFilter(familyFilter);

        //查询
        ResultScanner results = table.getScanner(scan);

        parseResults(results);
    }

    /**
     * 值过滤器
     * 获取值等于  大乔的数据
     *
     * @throws IOException
     */
    @Test
    public void ValueFilter() throws IOException {

        //创建值过滤器对象
        //参数:final CompareOp familyCompareOp,
        //    final ByteArrayComparable familyComparator  是一个抽象类  使用子类SubstringComparator 模糊匹配
        ValueFilter valueFilter = new ValueFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator("乔"));
        //创建scan对象
        Scan scan = new Scan();
        scan.setFilter(valueFilter);

        //查询
        ResultScanner results = table.getScanner(scan);

        parseResults(results);
    }

    /**
     * 单列值过滤器 : 返回满足条件的这一行所有列的数据  需要指定:列族 列值
     * 单列值排除过滤器 : 排除满足条件的列  其他的列全部返回
     *
     * @throws IOException
     */
    @Test
    public void singleColumnValueFilter() throws IOException {

        //创建单列值过滤器对象
        //final byte [] family,
        // final byte [] qualifier,
        // final CompareOp compareOp,
        // final ByteArrayComparable comparator
        //SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter("f1".getBytes(), "name".getBytes(), CompareFilter.CompareOp.EQUAL, "大乔".getBytes());
        SingleColumnValueExcludeFilter singleColumnValueExcludeFilter = new SingleColumnValueExcludeFilter("f1".getBytes(), "name".getBytes(), CompareFilter.CompareOp.EQUAL, "大乔".getBytes());

        getScanAndparseResults(singleColumnValueExcludeFilter);
    }

    /**
     * 综合过滤器  并的关系
     * 1.row过滤器 过滤小于0003
     * 2.列族过滤器 过滤等于name
     * 3.列值过滤器  值等于大乔
     *
     * @throws IOException
     */
    @Test
    public void filterList() throws IOException {

        //1.row过滤器 过滤小于0003
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.LESS, new BinaryComparator("0003".getBytes()));
        //2.列族过滤器 过滤等于name

        //3.列值过滤器  值等于大乔

    }


    /**
     * 封装方法  解析查询的一行结果输出
     *
     * @param result
     */
    public void parseCell(Result result) {

        //通过将查询的一行数据 获取为cell在输出
        //获取cell数组 里面就是一个单元格包含的数据  一个列值得数据 包含rk 列族 列名 列值
        Cell[] cells = result.rawCells();

        for (Cell cell : cells) {

            //工具类来获取
            byte[] row = CellUtil.cloneRow(cell);
            byte[] family = CellUtil.cloneFamily(cell);
            byte[] qualifier = CellUtil.cloneQualifier(cell);
            byte[] value = CellUtil.cloneValue(cell);

            System.out.print("row = " + Bytes.toString(row) + "\t");
            System.out.print("family = " + Bytes.toString(family) + "\t");
            System.out.print("qualifier = " + Bytes.toString(qualifier) + "\t");

            //判断value值 如果是int类型的 id 和age  需要转化为int
            //int转化为string会报错
            //获取列名
            String column = Bytes.toString(qualifier);
            if (column.equals("id") || column.equals("age")) {
                System.out.print("value = " + Bytes.toInt(value) + "\t");
            } else {
                System.out.print("value = " + Bytes.toString(value) + "\t");
            }

            System.out.println();
            System.out.println("------------------------------------------------------------------------------");
        }
    }

    public void parseResults(ResultScanner results) {
        for (Result result : results) {
            parseCell(result);
        }
    }

    public void getScanAndparseResults(Filter filter) throws IOException {
        //创建scan对象
        Scan scan = new Scan();
        scan.setFilter(filter);

        //查询
        ResultScanner results = table.getScanner(scan);

        parseResults(results);
    }


}
