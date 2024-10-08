package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ProfileWindow extends JFrame {
    public ProfileWindow(ChatWindow chatWindow, Socket socket) {
        setTitle("完善个人信息");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 添加个人信息输入组件
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JLabel genderLabel = new JLabel("性别:");
        JTextField genderField = new JTextField();
        panel.add(genderLabel);
        panel.add(genderField);

        JLabel hobbyLabel = new JLabel("爱好:");
        JTextField hobbyField = new JTextField();
        panel.add(hobbyLabel);
        panel.add(hobbyField);

        JLabel birthdayLabel = new JLabel("出生日期:");
        JTextField birthdayField = new JTextField();
        panel.add(birthdayLabel);
        panel.add(birthdayField);

        JLabel bioLabel = new JLabel("个人简介:");
        JTextField bioField = new JTextField();
        panel.add(bioLabel);
        panel.add(bioField);

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> {
            //获取用户输入
            String gender = genderField.getText();
            String hobby = hobbyField.getText();
            String birthday = birthdayField.getText();
            String bio = bioField.getText();
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(8);//发送完善个人信息请求
                dos.writeUTF(gender);
                dos.writeUTF(hobby);
                dos.writeUTF(birthday);
                dos.writeUTF(bio);
                dos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // 保存个人信息
            System.out.println("保存个人信息");
            dispose();
        });

        panel.add(new JLabel()); // 占位符
        panel.add(saveButton);

        add(panel);
    }
}