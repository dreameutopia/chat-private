import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class configure extends JFrame {
    private JTextField serverIpField;  // 服务器IP地址输入框
    private JTextField serverPortField; // 服务器端口输入框

    private String serverIp;
    private int serverPort;

    public configure() {
        // 初始化组件
        initializeComponents();

        // 设置窗体属性
        setTitle("控制面板"); // 设置窗体标题
        setSize(400, 200); // 调整窗体大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗体时退出程序
        setLocationRelativeTo(null); // 窗体居中显示
    }

    private void initializeComponents() {
        // 使用GridBagLayout进行布局
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 设置组件之间的间距

        // 添加组件
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("服务器IP地址:"), gbc); // 添加服务器IP地址标签
        gbc.gridx = 1;
        serverIpField = new JTextField(20); // 创建服务器IP地址输入框
        add(serverIpField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("服务器端口:"), gbc); // 添加服务器端口标签
        gbc.gridx = 1;
        serverPortField = new JTextField(20); // 创建服务器端口输入框
        add(serverPortField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2; // 按钮占据两列
        JButton saveButton = new JButton("保存设置"); // 创建保存设置按钮
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings(); // 点击按钮时调用保存设置方法
            }
        });
        add(saveButton, gbc);
    }

    private void saveSettings() {
        String serverIp = serverIpField.getText().trim();
        String serverPortStr = serverPortField.getText().trim();

        if (serverIp.isEmpty() || serverPortStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "所有必填项都不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int serverPort;
        try {
            serverPort = Integer.parseInt(serverPortStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.serverIp = serverIp;
        this.serverPort = serverPort;

        generateConstantFile();
    }

    private void generateConstantFile() {
        String filePath = "chatusers/src/cn/dreameutopia/ui/constant.java"; // 假设文件路径
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        // 确保父目录存在
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("无法创建目录: " + parentDir.getAbsolutePath());
                return;
            }
        }

        // 如果文件存在，则删除
        if (file.exists()) {
            System.out.println("文件已存在，将删除: " + filePath);
            if (!file.delete()) {
                System.err.println("无法删除现有文件: " + filePath);
                return;
            }
            System.out.println("已删除现有文件: " + filePath);
        }

        // 存放通信端口号和服务器IP地址
        String content = String.format(
                "public class Constant {\n" +
                "    public static final String server_ip = \"%s\";\n" +
                "    public static final int port = %d;\n" +
                "}\n",
                serverIp, serverPort
        );

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            System.out.println("文件创建成功: " + filePath);
        } catch (IOException e) {
            System.err.println("创建文件时发生错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configure frame = new configure(); // 创建窗体实例
            frame.setVisible(true); // 显示窗体
        });
    }
}