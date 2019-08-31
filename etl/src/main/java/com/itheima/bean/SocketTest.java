package com.itheima.bean;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @program: etl
 * @description:
 * @author: Mr.Jiang
 * @create: 2019-08-18 17:46
 **/
public class SocketTest {

    public static void main(String[] args) throws IOException {

        //通过服务端ip和端口号 向服务器发送信息
        //创建客户端连接对象
        Socket client = new Socket("node01", 8888);

        //通过printWriter打印输出流 发送数据
        //参数:输出数据 需要输出流OutputStream out
        //通过client对象获取输出流
        OutputStream outputStream = client.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);

        for (int i = 0; i < 10; i++) {

            //发送消息
            printWriter.println("这是第"+i+"条消息...");
            printWriter.flush();
        }
    }
}
