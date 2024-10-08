package cn.dreameutopia.ui;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class clientReader extends Thread{
    private Socket socket;
    private DataInputStream dis;
    private ChatWindow win;
    clientReader(Socket s, ChatWindow win){
            this.socket=s;
            this.win=win;
    }
    @Override
    public void run(){
        try {
            //接受的消息有很多种类型 1在线人数更新  2 群聊消息  3 私聊消息

            DataInputStream dis=new DataInputStream(socket.getInputStream());
            while (true) {
                int type=dis.readInt();
                System.out.println("type:"+type);
                switch (type){
                    case 1:
                        //读取有多少个在线用户
                        //System.out.println("获取到人数");
                        int count=dis.readInt();
                        //System.out.println("获取到人数");
                        //循环控制读取多少个用户信息
                        List<String> onlinelist=new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            String nickname=dis.readUTF();
                            //将每个用户添加到集合中
                            onlinelist.add(nickname);

                        }
                        //System.out.println("获取到名字");
                        //将集合中的数据同步到窗口上
                        win.updateOnlineUsers(onlinelist);


                        break;
                    case 2:
                        //System.out.println("接到消息------------");
                        String msg = dis.readUTF();
                        System.out.println(msg);
                        win.setMsgToWin(2,msg);

                        //服务端发来的群聊消息

                        break;
                    case 3:
                        //客户端发来了私聊消息，再把消息转发给指定客户端

                        String msg3=dis.readUTF();
                        System.out.println("接受到私聊信息"+msg3);

                        win.setMsgToWin(3,msg3);
                        break;
                    case 4:
                        try {

                            while (true) {
                                String date = dis.readUTF();
                                if (date.equals("over")) {
                                    break;
                                }
                                String data = dis.readUTF();

                                System.out.println(date+"  " + data);
                                System.out.println("添加到map");
                                win.getChatMap().put(date,data);
                                // 处理数据
                            }
                            //win.setMsgToWin(4,msg3);
                            System.out.println("获取历史数据完毕");
                            win.initcomplete=true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5:
                        //获取到对方个人信息
                        System.out.println("正在从服务器获取到个人信息");
                        int count3=dis.readInt();
                        if(count3==6){
                            String name=dis.readUTF();
                            String gender =dis.readUTF();
                            String hobby=dis.readUTF();
                            String birthday=dis.readUTF();
                            String bio=dis.readUTF();
                            System.out.println("接受到个人信息"+name+" "+gender+" "+hobby+" "+birthday+" "+bio);
                            win.showuserinfo(name,gender,hobby,birthday,bio);
                        }


                        break;

                    default:
                        System.out.println("未知类型");
                        break;
                }
                // 确保数据流完全读取
                dis.skipBytes(dis.available());
            }

        } catch (Exception e) {

           e.printStackTrace();
        }
    }



    //更新客户端的在线人数列表
    private void updateclientonlineusers() throws Exception {
        //读取有多少个在线用户
        //System.out.println("获取到人数");
        int count=dis.readInt();
        //System.out.println("获取到人数");
        //循环控制读取多少个用户信息
        List<String> onlinelist=new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String nickname=dis.readUTF();
            //将每个用户添加到集合中
            onlinelist.add(nickname);

        }
        System.out.println("获取到名字");
        //将集合中的数据同步到窗口上
        win.updateOnlineUsers(onlinelist);


    }


}
