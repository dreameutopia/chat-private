package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RegisterForm extends JFrame {
    private JTextField nicknameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private ChatEntryForm parentForm; // 保存父窗体引用
    private Socket socket;

    public RegisterForm(ChatEntryForm parentForm, Socket S1) {
        this.parentForm = parentForm;
        socket=S1;
        initialize();
        this.setVisible(true);
    }

    private void initialize() {
        setTitle("局域网聊天 - 注册界面");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        // 按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setBackground(new Color(0x4CAF50));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);

        // 按钮监听器
        registerButton.addActionListener(e -> {
            String nickname = nicknameField.getText();
            char[] password = passwordField.getPassword();
            if (nickname.trim().length() > 0 && password.length > 0) {
                System.out.println("注册昵称: " + nickname);
                System.out.println("注册密码: " + new String(password)); // 注意：实际应用中不要直接打印密码

                try {
                    // 发送注册信息给服务器
                    registertoserver(nickname, new String(password));
                    DataInputStream dis=new DataInputStream(socket.getInputStream());
                    int type=dis.readInt();
                    if(type==10){
                        JOptionPane.showMessageDialog(RegisterForm.this, "这个账户已经被注册", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        // 假设注册成功
                        JOptionPane.showMessageDialog(RegisterForm.this, "注册成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // 关闭注册窗口
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                }

            } else {
                JOptionPane.showMessageDialog(RegisterForm.this, "请输入您的昵称和密码!", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLocationRelativeTo(null);
    }

    private void registertoserver(String nickname, String password) throws IOException {
        // 检查socket是否已经创建并且连接状态为true
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            // 如果已经连接，则直接使用现有的socket
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(5); // 消息类型  登录
            dos.writeUTF(nickname);
            dos.writeUTF(password); // 发送密码
            dos.flush();
        } else {
            // 如果没有连接或者连接已关闭，则重新创建新的socket连接
            socket = new Socket(constant.server_ip, constant.port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(5); // 消息类型  登录
            dos.writeUTF(nickname);
            dos.writeUTF(password); // 发送密码
            dos.flush();
        }
    }
}