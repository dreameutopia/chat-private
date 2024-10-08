package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ViewProfileWindow extends JFrame {
    public ViewProfileWindow(String user, ChatWindow chatWindow) {
        String gender= null;
        String hobby= null;
        String birthday= null;
        String bio= null;
        try {
            chatWindow.sendquerytoserver(user);
            setTitle("查看 " + user + " 的个人信息");
            Thread.sleep(500);
            if(chatWindow.getUserinfo()==null){
                JOptionPane.showMessageDialog(this, "该用户还未填写信息");
                //关闭窗口
                dispose();
                return;
            }
            List<String> userinfo = chatWindow.getUserinfo();

            gender = userinfo.get(1);
            hobby = userinfo.get(2);
            birthday = userinfo.get(3);
            bio = userinfo.get(4);
            chatWindow.claerUserinfo();
        } catch (Exception e) {
            e.printStackTrace();
        }


        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 初始化个人信息显示组件
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JLabel genderLabel = new JLabel("性别:");
        JLabel genderValue = new JLabel(gender);
        panel.add(genderLabel);
        panel.add(genderValue);

        JLabel hobbyLabel = new JLabel("爱好:");
        JLabel hobbyValue = new JLabel(hobby);
        panel.add(hobbyLabel);
        panel.add(hobbyValue);

        JLabel birthdayLabel = new JLabel("出生日期:");
        JLabel birthdayValue = new JLabel(birthday);
        panel.add(birthdayLabel);
        panel.add(birthdayValue);

        JLabel bioLabel = new JLabel("个人简介:");
        JLabel bioValue = new JLabel(bio);
        panel.add(bioLabel);
        panel.add(bioValue);

        add(panel);
        setVisible(true); // 显示窗口
    }

    private void sendquerytoserver() {
    }


}