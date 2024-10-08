package cn.dreameutopia.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class server {
    //定义一个容器存储所有登录进来的客户端管道，以便群发消息
    //定义一个map集合   键是存储客户端管道，值存储用户名称
    public static final Map<Socket,String> onlinesocket=new HashMap<>();

    public static void main(String[] args) {
        System.out.println("-------------启动服务端系统-----------------");
        try {
            //注册端口
            ServerSocket serverSocket=new ServerSocket(constant.port);
            //主线程负责接收客户端连接请求
            while(true){
                System.out.println("等待客户端连接。。。。");
                Socket socket = serverSocket.accept();
                new ServerReader(socket).start();

                System.out.println("成功接入一个客户端");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
