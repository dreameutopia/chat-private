package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChangePasswordForm extends JFrame {
    private JTextField nicknameField;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changeButton;
    private Socket socket;
    private ChatEntryForm parentForm;

    public ChangePasswordForm(ChatEntryForm parentForm, Socket socket) {
        try {
            this.parentForm = parentForm;

            if(socket==null){
                System.out.println("管道为空，创建新管道");
                socket = new Socket(constant.server_ip, constant.port);
                this.socket = socket;
            }
            else{
                this.socket = socket;
            }

            initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialize() {
        setTitle("修改密码");
        setSize(400, 300);
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

        // 旧密码标签
        JLabel oldPasswordLabel = new JLabel("旧密码:");
        oldPasswordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(oldPasswordLabel, gbc);

        // 旧密码字段
        oldPasswordField = new JPasswordField();
        oldPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(oldPasswordField, gbc);
        oldPasswordField.setColumns(10);

        // 新密码标签
        JLabel newPasswordLabel = new JLabel("新密码:");
        newPasswordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(newPasswordLabel, gbc);

        // 新密码字段
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(newPasswordField, gbc);
        newPasswordField.setColumns(10);

        // 确认新密码标签
        JLabel confirmPasswordLabel = new JLabel("确认新密码:");
        confirmPasswordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(confirmPasswordLabel, gbc);

        // 确认新密码字段
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(confirmPasswordField, gbc);
        confirmPasswordField.setColumns(10);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        changeButton = new JButton("修改密码");
        changeButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        changeButton.setBackground(new Color(0x2196F3));
        changeButton.setForeground(Color.WHITE);
        changeButton.setFocusPainted(false);
        buttonPanel.add(changeButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        add(buttonPanel, gbc);

        // 修改密码按钮监听器
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText();
                char[] oldPassword = oldPasswordField.getPassword();
                char[] newPassword = newPasswordField.getPassword();
                char[] confirmPassword = confirmPasswordField.getPassword();

                if (nickname.trim().length() > 0 && oldPassword.length > 0 && newPassword.length > 0 && confirmPassword.length > 0) {
                    if (new String(newPassword).equals(new String(confirmPassword))) {
                        try {
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            System.out.println("发送了修改密码请求");
                            dos.writeInt(6); // 消息类型  修改密码
                            dos.writeUTF(nickname);
                            dos.writeUTF(new String(oldPassword));
                            dos.writeUTF(new String(newPassword));
                            dos.flush();

                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            int type = dis.readInt();
                            if (type == 9) { // 修改密码失败
                                JOptionPane.showMessageDialog(ChangePasswordForm.this, "旧密码错误或修改失败，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(ChangePasswordForm.this, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                                dispose();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        JOptionPane.showMessageDialog(ChangePasswordForm.this, "新密码和确认新密码不一致，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(ChangePasswordForm.this, "请输入完整的信息！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setLocationRelativeTo(parentForm);
    }
}