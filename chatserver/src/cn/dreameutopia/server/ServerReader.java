package cn.dreameutopia.server;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServerReader extends Thread{
    private Socket socket;
    public static final Map<String,String> name_password=new HashMap<>();
    private String name;
    private DataSource dataSource;
    private Connection conn;
    ServerReader(Socket s){
        try {
            Properties prop=new Properties();
            prop.load(new FileInputStream(constant.PROPERT_PATH));
            this.dataSource = DruidDataSourceFactory.createDataSource(prop);

            conn= dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket=s;
    }
    @Override
    public void run(){
        try {
            //接受的消息有很多种类型 1登录类型  2 群聊消息  3 私聊消息
            //所以客户端必须申明协议发送消息
            //比如客户端先发1  代表接下来是登录消息
            //比如客户端先发1  代表接下来是群聊消息
            // 先接收一个数字  在判断，先从输入管道中接受客户端发送来的消息类型编号
            DataInputStream dis=new DataInputStream(socket.getInputStream());
            while (true) {
                int type=dis.readInt();
                switch (type){
                    case 1:
                        //客户端发来了登录消息，再更新全部在线人数列表
                        String nickname=dis.readUTF();
                        name=nickname;
                        String password=dis.readUTF();
                        System.out.println(nickname+"登录了");
                        System.out.println("开始比对--------------");
                        if(!requestiscorrect(nickname,password)){
                            DataOutputStream dos1=new DataOutputStream(socket.getOutputStream());
                            dos1.writeInt(6);//登录失败
                            continue;
                        }
                        DataOutputStream dos1=new DataOutputStream(socket.getOutputStream());
                        dos1.writeInt(7);//登录成功
                        //把这个登录成功的客户端socket存入到在线集合
                        server.onlinesocket.put(socket,nickname);

                        //更新全部客户端在线人数列表
                        updatesocketonlinelist();
                        break;
                    case 2:
                        //客户端发来了群聊消息，再把消息转发给其他所有客户端
                        String msg=dis.readUTF();
                        sendmessagetoall(msg);
                        break;
                    case 3:
                        //客户端发来了私聊消息，再把消息转发给指定客户端
                        String to_nickname=dis.readUTF();
                        System.out.println("接收到私聊消息，发送至："+to_nickname);
                        String msg3=dis.readUTF();
                        //根据用户名查找到相应的管道去发送消息
                        for (Map.Entry<Socket,String> entry : server.onlinesocket.entrySet()) {
                            try {
                                System.out.println(entry.getValue());
                                System.out.println("对比"+to_nickname);
                                if(entry.getValue().equals(to_nickname)){
                                    //把用户名发送给客户端
                                    DataOutputStream dos=new DataOutputStream(entry.getKey().getOutputStream());
                                    System.out.println("匹配成功，正在发送消息");
                                    dos.writeInt(3);//1代表告诉客户端接下来是在线人数列表信息    2代表发送群聊消息
                                    StringBuilder sb2=new StringBuilder();

                                    //获取当前时间
                                    LocalDateTime now=LocalDateTime.now();
                                    DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEE a");
                                    String nowstr=dtf.format(now);
                                    //拼接字符串
                                    StringBuilder msgresult=sb2.append(name).append(" ").append(nowstr).append("\r\n").append(msg3).append("\r\n");
                                    dos.writeUTF(msgresult.toString());
                                    dos.flush();
                                }

                            } catch (Exception e) {}
                        }
                        break;
                    case 5:
                        //客户端发来了注册请求，存储到内部文件中
                        String username=dis.readUTF();
                        String userpassword=dis.readUTF();
                        registerforusers(username,userpassword);
                        break;
                    case 6:
                        //修改密码请求
                        String changeusername=dis.readUTF();
                        String oldpassword=dis.readUTF();
                        String newpassword=dis.readUTF();
                        System.out.println("修改密码请求"+changeusername+" "+oldpassword+" "+newpassword);
                        changepasseord(changeusername,oldpassword,newpassword);
                    case 8:
                        System.out.println("接收到完善个人信息请求");
                        String gender=dis.readUTF();
                        String hobby=dis.readUTF();
                        String birthday=dis.readUTF();
                        String bio=dis.readUTF();
                        updatepersonalinfo(gender,hobby,birthday,bio);
                        break;
                    case 9:
                        //获取到申请全部聊天记录请求
                        try {
                            String sql="select * from chatlog;";
                            Statement stmt = conn.createStatement();

                            ResultSet RS=stmt.executeQuery(sql);

                            DataOutputStream dos2=new DataOutputStream(socket.getOutputStream());

                            dos2.writeInt(4);
                           // System.out.println("开始发送数据");
                            while (RS.next()) {
                                String name=RS.getString(1);
                                String time=RS.getString(2);
                                String msg2=RS.getString(3);

                                dos2.writeUTF(time);
                                dos2.writeUTF(name+","+msg2);
                                dos2.flush();

                            }
                            //System.out.println("数据发送完毕");
                            dos2.writeUTF("over");
                            dos2.flush();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 10:
                        String queryusername=dis.readUTF();
                        queryuserinfo(queryusername);
                        break;


                    default:
                        break;
                }
            }

        } catch (Exception e) {

            System.out.println("客户端断开连接了"+socket.getInetAddress().getHostAddress());
            server.onlinesocket.remove(socket);
            updatesocketonlinelist();
        }
    }

    private void queryuserinfo(String queryusername) {
        try {
            DataOutputStream dos2=new DataOutputStream(socket.getOutputStream());
            dos2.writeInt(5);//准备发送回复信息
            String sql="select * from personalinfo where name=?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, queryusername);
            ResultSet RS=stmt.executeQuery();
            if(RS.next()){
                //如果存在，那么就返回数值
                dos2.writeInt(6);
                dos2.writeUTF(RS.getString(1));
                dos2.writeUTF(RS.getString(2));
                dos2.writeUTF(RS.getString(3));
                dos2.writeUTF(RS.getString(4));
                dos2.writeUTF(RS.getString(5));
                dos2.flush();

            }
            else{
                dos2.writeInt(7);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatepersonalinfo(String gender, String hobby, String birthday, String bio) {
        try {
            //System.out.println(name+" "+gender+" "+hobby+" "+birthday+" "+bio);
            //判断是否已经有数据，如果有数据那就直接进行修改
            String sql="select * from personalinfo where name=?;";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet RS=stmt.executeQuery();
            if(RS.next()){
                //说明已经有数据了，那就直接修改
                String sql2="update personalinfo set gender=?,hobby=?,birthday=?,bio=? where name=?;";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setString(1, gender);
                stmt2.setString(2, hobby);
                stmt2.setString(3, birthday);
                stmt2.setString(4, bio);
                stmt2.setString(5, name);
                int count = stmt2.executeUpdate();
                if(count>0){
                    System.out.println("更新个人信息成功");
                }else{
                    System.out.println("更新个人信息失败");
                }
            }else{
                String sql1="insert into personalinfo (name,gender,hobby,birthday,bio) values (?,?,?,?,?);";
                PreparedStatement stmt1 = conn.prepareStatement(sql1);
                stmt1.setString(1, name);
                stmt1.setString(2, gender);
                stmt1.setString(3, hobby);
                stmt1.setString(4, birthday);
                stmt1.setString(5, bio);
                int count = stmt1.executeUpdate();
                if(count>0){
                    System.out.println("创建个人信息成功");
                }else{
                    System.out.println("创建个人信息失败");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllMessage() {



    }

    private void changepasseord(String changeusername, String oldpassword, String newpassword) {
        try {
            //创建statement对象
            String sql1="select * from checklog where user_name=? and pass_word=?;";
            PreparedStatement stmt1 = conn.prepareStatement(sql1);

            stmt1.setString(1, changeusername);
            stmt1.setString(2, oldpassword);
            ResultSet rs1 = stmt1.executeQuery();
            if(rs1.next()){
                //可以修改密码

                System.out.println("可以修改密码");

            }else{

                System.out.println("不可以修改密码");
            }

            String sql="update checklog set pass_word=? where user_name=? and pass_word=?;";

             //获取PreparedStatement  并且预编译语句
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newpassword);
            stmt.setString(2, changeusername);
            stmt.setString(3, oldpassword);
            int account = stmt.executeUpdate();
            if(account==1){
                //修改成功
                DataOutputStream dos1=new DataOutputStream(socket.getOutputStream());
                dos1.writeInt(8);//修改成功
                System.out.println("修改密码成功");
                dos1.flush();
            }else{
                //修改失败
                DataOutputStream dos1=new DataOutputStream(socket.getOutputStream());
                dos1.writeInt(9);//修改失败
                System.out.println("修改密码失败");
                dos1.flush();
            }
            System.out.println("修改密码过程结束");
            //关闭资源
            stmt.close();
            conn.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private boolean requestiscorrect(String nickname, String userpassword) {
        //System.out.println("更新数据启动");
        updateusernamepassword();
        //System.out.println("更新数据完成");

        for (Map.Entry<String, String> entry : name_password.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //System.out.println(nickname+"+"+userpassword+"------"+key+"+"+value);
            if(nickname.equals(key) && userpassword.equals(value)){
                return true;
            }
        }
        //System.out.println("比对失败");
        return false;

    }
    private void updateusernamepassword(){
        try {
            //从数据库查询到所有用户的昵称和密码
            //写到数据库中去
            //定义连接
            Connection conn= dataSource.getConnection();

            //创建statement对象
            Statement stmt = conn.createStatement();
            String sql="select * from checklog;";

            ResultSet rs=stmt.executeQuery(sql);
            while(rs.next()){
                //获取数据
                //第一种方式，直接写出数据表的列名  推荐

                String name=rs.getString("user_name");
                String password=rs.getString("pass_word");

                name_password.put(name, password);
               // System.out.println("读取到数据 "+name+"+"+password);

            }
            rs.close();
            stmt.close();
            conn.close();


        } catch (Exception e) {
            e.printStackTrace();
        }





    }
    private void registerforusers(String username, String password) {

        try {
            //检查昵称是否已经存在
            String sql1="select * from checklog where user_name=?;";
            //获取PreparedStatement  并且预编译语句
            PreparedStatement stmt2 = conn.prepareStatement(sql1);
            stmt2.setString(1, username);
            ResultSet rs1 = stmt2.executeQuery();
            DataOutputStream dos1=new DataOutputStream(socket.getOutputStream());
            if(rs1.next()){
                //已经存在

                dos1.writeInt(10);//已经存在
                System.out.println("已经存在");
                dos1.flush();
                return;
            }
            String sql="insert into checklog(user_name,pass_word) values(?,?)";
            //获取PreparedStatement  并且预编译语句
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);


            int account = stmt.executeUpdate();

            if(account>0){
                dos1.writeInt(11);

                dos1.flush();
                System.out.println("注册成功");
            }
            //释放资源

            stmt.close();
            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    //给全部在线socket推送当前客户端发来的消息
    private void sendmessagetoall(String msg) {
        //一定要拼装好消息再发给全部在线socket
        StringBuilder sb=new StringBuilder();
        String name=server.onlinesocket.get(socket);
        //获取当前时间
        LocalDateTime now=LocalDateTime.now();
        DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEE a");
        String nowstr=dtf.format(now);

        try {
            //把数据保留在数据库中


            //创建statement对象
            String sql1="insert into chatlog(name,time,message) VALUES (?,?,?)";
            PreparedStatement stmt1 = conn.prepareStatement(sql1);
            stmt1.setString(1, name);
            stmt1.setString(2, nowstr);
            stmt1.setString(3, msg);


            int account = stmt1.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //拼接字符串
        StringBuilder msgresult=sb.append(name).append(" ").append(nowstr).append("\r\n").append(msg).append("\r\n");
        //推送给全部客户端socket
        for (Socket socket : server.onlinesocket.keySet()) {
            try {
                //把用户名发送给客户端
                //System.out.println("-----------------------------------------------");
                DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
                dos.writeInt(2);//1代表告诉客户端接下来是在线人数列表信息    2代表发送群聊消息
                //System.out.println("发送了一个消息"+msgresult.toString());
                //System.out.println("-----------------------------------------------");
                dos.writeUTF(msgresult.toString());
                dos.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void updatesocketonlinelist() {
        //更新全部客户端在线人数列表
        //拿到全部在线客户端的名字，把名字发送给全部在线socket管道
        //先拿到当前全部在线用户名称

        Collection<String> onlineusers = server.onlinesocket.values();
        //把这个集合中所有用户都推送给全部客户端管道
        for (Socket socket : server.onlinesocket.keySet()) {
            try {
                //把用户名发送给客户端
                DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
                dos.writeInt(1);//1代表告诉客户端接下来是在线人数列表信息    2代表发送群聊消息
                dos.writeInt(onlineusers.size());
                //System.out.println("同步中：当前在线人数："+onlineusers.size());
                for(String onlineuser : onlineusers){
                    dos.writeUTF(onlineuser);
                }
                dos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
