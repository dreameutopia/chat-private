package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatEntryForm extends JFrame {
    private JTextField nicknameField;
    private JPasswordField passwordField; // 新增密码输入框
    private JButton enterButton;
    private JButton registerButton; // 更改按钮名称
    private JButton changePasswordButton; // 新增修改密码按钮
    private Socket socket;

    public ChatEntryForm() {
        initialize();
        this.setVisible(true);
    }

    private void initialize() {
        setTitle("局域网聊天 - 进入界面");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 标签
        JLabel label = new JLabel("昵称:");
        label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(label, gbc);

        // 文本字段
        nicknameField = new JTextField();
        nicknameField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(nicknameField, gbc);
        nicknameField.setColumns(10);

        // 密码标签
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        // 密码字段
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(passwordField, gbc);
        passwordField.setColumns(10);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        enterButton = new JButton("登录");
        enterButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        enterButton.setBackground(new Color(0x4CAF50));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFocusPainted(false);
        buttonPanel.add(enterButton);

        registerButton = new JButton("注册"); // 更改按钮文本
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setBackground(new Color(0xF44336));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        buttonPanel.add(registerButton);

        changePasswordButton = new JButton("修改密码"); // 新增修改密码按钮
        changePasswordButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        changePasswordButton.setBackground(new Color(0x2196F3));
        changePasswordButton.setForeground(Color.WHITE);
        changePasswordButton.setFocusPainted(false);
        buttonPanel.add(changePasswordButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        add(buttonPanel, gbc);

        // 登录按钮监听器
        enterButton.addActionListener(e -> {
            String nickname = nicknameField.getText();
            char[] password = passwordField.getPassword();
            try {
                socket = new Socket(constant.server_ip, constant.port);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(1); // 消息类型  登录
                dos.writeUTF(nickname);
                dos.writeUTF(new String(password)); // 发送密码
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("正在尝试这次登录");
            if (checkissucces(nickname, new String(password))) {
                System.out.println("登录成功");
            } else {
                System.out.println("失败的尝试");
                return;
            }
            if (nickname.trim().length() > 0 && password.length > 0) {
                try {
                    clientLogin(nickname, new String(password));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ChatWindow chatWindow = new ChatWindow(nickname, socket);
                chatWindow.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(ChatEntryForm.this, "请输入您的昵称和密码!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 注册界面按键
        registerButton.addActionListener(e -> {
            RegisterForm registerForm = new RegisterForm(this, socket); // 传入当前登录窗体实例
            registerForm.setVisible(true);
        });

        // 修改密码按钮监听器
        changePasswordButton.addActionListener(e -> {
            ChangePasswordForm changePasswordForm = new ChangePasswordForm(this, socket); // 传入当前登录窗体实例
            changePasswordForm.setVisible(true);
        });

        setLocationRelativeTo(null);
    }

    private boolean checkissucces(String nickname, String s) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int type = dis.readInt();
            if (type == 6) { // 登录失败
                JOptionPane.showMessageDialog(ChatEntryForm.this, "用户名或密码错误，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean clientLogin(String nickname, String password) throws Exception {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(1); // 消息类型  登录
        dos.writeUTF(nickname);
        dos.writeUTF(password); // 发送密码
        dos.flush();
        return true;
    }
}