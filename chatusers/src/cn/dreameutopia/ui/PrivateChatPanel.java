package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrivateChatPanel extends JPanel {
    private JTextArea privateMessageArea;
    private JTextField privateMessageField;
    private JButton privateSendButton;
    private JButton backButton;
    private JButton editProfileButton;
    private JButton viewProfileButton;
    private String currentPrivateChatUser;
    private Socket socket;
    private ChatWindow chatWindow;

    public PrivateChatPanel(Socket socket, ChatWindow chatWindow) {
        this.socket = socket;
        this.chatWindow = chatWindow;
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // 私聊消息显示区域
        privateMessageArea = new JTextArea();
        privateMessageArea.setEditable(false);
        privateMessageArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        privateMessageArea.setLineWrap(true);
        privateMessageArea.setWrapStyleWord(true);
        JScrollPane privateMessageScrollPane = new JScrollPane(privateMessageArea);

        // 输入面板
        JPanel privateInputPanel = new JPanel();
        privateInputPanel.setBackground(Color.WHITE);
        privateInputPanel.setLayout(new BorderLayout());

        privateMessageField = new JTextField();
        privateMessageField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        privateInputPanel.add(privateMessageField, BorderLayout.CENTER);

        privateSendButton = new JButton("私聊");
        privateSendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        privateSendButton.setBackground(new Color(0x4CAF50));
        privateSendButton.setForeground(Color.WHITE);
        privateSendButton.setFocusPainted(false);
        privateInputPanel.add(privateSendButton, BorderLayout.EAST);

        add(privateMessageScrollPane, BorderLayout.CENTER);
        add(privateInputPanel, BorderLayout.SOUTH);

        // 返回按钮
        backButton = new JButton("当前为私聊界面，点击返回主界面");
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        backButton.setBackground(new Color(0xF44336));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> chatWindow.switchToGroupChat());

        // 编辑个人信息按钮
        editProfileButton = new JButton("完善个人信息");
        editProfileButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        editProfileButton.setBackground(new Color(0x2196F3));
        editProfileButton.setForeground(Color.WHITE);
        editProfileButton.setFocusPainted(false);
        editProfileButton.addActionListener(e -> openEditProfileWindow());

        // 查看对方个人信息按钮
        viewProfileButton = new JButton("查询对方个人信息");
        viewProfileButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        viewProfileButton.setBackground(new Color(0x9C27B0));
        viewProfileButton.setForeground(Color.WHITE);
        viewProfileButton.setFocusPainted(false);
        viewProfileButton.addActionListener(e -> openViewProfileWindow());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(backButton);
        buttonPanel.add(editProfileButton);
        buttonPanel.add(viewProfileButton);
        add(buttonPanel, BorderLayout.NORTH);

        // 按钮监听器
        privateSendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = privateMessageField.getText();
                privateMessageField.setText("");
                // 发送私聊消息
                StringBuilder sb2 = new StringBuilder();
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEE a");
                String nowstr = dtf.format(now);
                // 拼接字符串
                StringBuilder msgresult = sb2.append("我").append(" ").append(nowstr).append("\r\n").append(message).append("\r\n").append("\r\n");

                privateMessageArea.append(msgresult.toString());
                sendMessageToServer(3, message);
            }
        });
    }

    public void setCurrentPrivateChatUser(String user) {
        this.currentPrivateChatUser = user;
        privateMessageArea.setText(""); // 清空私聊消息区域
    }

    private void sendMessageToServer(int type, String message) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(type);
            if (type == 3) {
                dos.writeUTF(currentPrivateChatUser); // 目标用户
            }
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JTextArea getPrivateMessageArea() {
        return privateMessageArea;
    }

    public String getCurrentPrivateChatUser() {
        return currentPrivateChatUser;
    }

    private void openEditProfileWindow() {
        // 打开完善个人信息窗口
        ProfileWindow profileWindow = new ProfileWindow(chatWindow,socket);
        profileWindow.setVisible(true);
    }

    private void openViewProfileWindow() {
        // 打开查看对方个人信息窗口
        ViewProfileWindow viewProfileWindow = new ViewProfileWindow(currentPrivateChatUser, chatWindow);

    }
}