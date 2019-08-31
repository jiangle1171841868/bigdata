package com.itheima.hbaseandes.utils;

import com.itheima.hbaseandes.bean.Article;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: etl
 * @description: 解析excle的工具类
 * 1.读取excle文件获取输入流
 * 2.获取sheet
 * 3.获取row
 * 4.获取cell  封装到bean中  在封装到list集合中
 * @author: Mr.Jiang
 * @create: 2019-08-18 19:33
 **/

public class ParseExcelUtil {

    //工具类 静态方法 可以通过方法名直接直接调用
    public static List<Article> parseExcle(String pathName) throws IOException {

        List<Article> articles = new ArrayList<>();
        /**
         * XSSFWorkbook ,解析 .xlsx后缀的excel表格  新格式 文件大
         * HSSFWorkbook ,解析 .xls后缀的excel  文件小
         */

        //获取excle文件输入流
        //参数:InputStream    抽象类 需要子类
        XSSFWorkbook xssfSheets = new XSSFWorkbook(new FileInputStream(new File(pathName)));

        //获取excle中的Sheet
        //根据索引获取
        XSSFSheet sheetAt = xssfSheets.getSheetAt(0);
        //根据sheet的名字获取
        //xssfSheets.getSheet(name)

        //获取shell的最后一行的行数  就是行数
        int lastRowNum = sheetAt.getLastRowNum();

        //从1开始遍历  第一行的列名不要 一直到最后一行 将数据封装到bean
        for (int i = 1; i <= lastRowNum; i++) {

            //根据索引获取一行的数据
            XSSFRow row = sheetAt.getRow(i);

            //获取单元格数据封装到bean
            String id = row.getCell(0).toString();
            String title = row.getCell(1).toString();
            String from = row.getCell(2).toString();
            String time = row.getCell(3).toString();
            String readCount = row.getCell(4).toString();
            String content = row.getCell(5).toString();

            //用article构造方法,封装数据
            Article article = new Article(id, title, from, time, readCount, content);
            articles.add(article);
        }
        return articles;
    }
}
