package com.itheima.hbaseandes.test;

import com.itheima.hbaseandes.bean.Article;
import com.itheima.hbaseandes.utils.EsUtil;
import com.itheima.hbaseandes.utils.HbaseUtil;
import com.itheima.hbaseandes.utils.ParseExcelUtil;

import java.io.IOException;
import java.util.List;

/**
 * @program: etl
 * @description:
 * @author: Mr.Jiang
 * @create: 2019-08-18 20:53
 **/
public class HbaseAndEsTest {

    public static void main(String[] args) throws IOException {

        String keyWord = "支付宝";
        //putData();

        queryData(keyWord);

    }

    public static void putData() throws IOException {
        //获取数据
        String pathName = "G:\\大数据\\14\\hbase\\day_04\\Day04_ELK\\elk-day02\\hbaseEs.xlsx";
        List<Article> articles = ParseExcelUtil.parseExcle(pathName);

        //将数据插入到hbase
        HbaseUtil.putData(articles);

        //将数据插入到es
        EsUtil.putData(articles);

    }

    public static void queryData(String keyWord) {

        List<Article> articles = EsUtil.queryByKeyWord(keyWord);
       // System.out.println("articles = " + articles);

       for (Article article : articles) {
            String content = HbaseUtil.queryByRowKey("articles", "article", "content", article.getId());
            System.out.println("content = " + content);

        }
    }
}
