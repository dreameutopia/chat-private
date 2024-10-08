import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ServerControlPanel extends JFrame {
    private JTextField serverPortField; // 服务器端口输入框
    private JTextField dbAddressField;  // 数据库地址输入框
    private JTextField dbPortField;     // 数据库端口输入框
    private JTextField dbNameField;     // 数据库名称输入框
    private JTextField dbUsernameField; // 数据库用户名输入框
    private JTextField dbPasswordField; // 数据库密码输入框
    private JTextField filePathField;   // 文件路径输入框
    private JTextField additionalPathField; // 环境变量路径输入框

    private int serverPort;
    private String dbAddress;
    private int dbPort;
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private String filePath;
    private String additionalPath;

    public ServerControlPanel() {
        initializeComponents();

        setTitle("服务端控制面板");
        setSize(400, 450); // 调整窗体大小以适应新增的输入框
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("服务器端口:"), gbc);
        gbc.gridx = 1;
        serverPortField = new JTextField(20);
        add(serverPortField, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("数据库地址:"), gbc);
        gbc.gridx = 1;
        dbAddressField = new JTextField(20);
        add(dbAddressField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("数据库端口:"), gbc);
        gbc.gridx = 1;
        dbPortField = new JTextField(20);
        add(dbPortField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("数据库名称:"), gbc);
        gbc.gridx = 1;
        dbNameField = new JTextField(20);
        add(dbNameField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        add(new JLabel("数据库用户名:"), gbc);
        gbc.gridx = 1;
        dbUsernameField = new JTextField(20);
        add(dbUsernameField, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        add(new JLabel("数据库密码:"), gbc);
        gbc.gridx = 1;
        dbPasswordField = new JPasswordField(20); // 使用JPasswordField来安全地处理密码输入
        add(dbPasswordField, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        add(new JLabel("配置文件路径:"), gbc);
        gbc.gridx = 1;
        filePathField = new JTextField(20);
        add(filePathField, gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        add(new JLabel("环境变量路径:"), gbc);
        gbc.gridx = 1;
        additionalPathField = new JTextField(20);
        add(additionalPathField, gbc);

        gbc.gridy = 8;
        gbc.gridwidth = 2;
        JButton saveButton = new JButton("保存设置");
        saveButton.addActionListener(e -> saveSettings());
        add(saveButton, gbc);
    }

    private void saveSettings() {
        String serverPortStr = serverPortField.getText();
        String dbAddress = dbAddressField.getText();
        String dbPortStr = dbPortField.getText();
        String dbName = dbNameField.getText();
        String dbUsername = dbUsernameField.getText();
        String dbPassword = dbPasswordField.getText(); // 从JPasswordField获取密码
        String filePath = filePathField.getText();
        String additionalPath = additionalPathField.getText();

        if (additionalPath == null || additionalPath.isEmpty()) {
            additionalPath = "chatserver/src/cn/dreameutopia/server/constant.java";
        }

        if (filePath == null || filePath.isEmpty()) {
            filePath = "chatserver/src/druid.properties";
        }

        if (serverPortStr == null || serverPortStr.isEmpty()) {
            //把6666转为字符串
            serverPort=6666;
            serverPortStr="6666";
            System.out.println("端口号未填写，默认为6666");
        }


        int serverPort;
        int dbPort;
        try {
            serverPort = Integer.parseInt(serverPortStr);
            dbPort = Integer.parseInt(dbPortStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "端口号必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //必须填写内容
        this.serverPort = serverPort;
        this.dbAddress = dbAddress;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;


        //自定初始化内容
        this.filePath = filePath;
        this.additionalPath = additionalPath;


        System.out.println("开始创建文件了");
        generateConstantFile();
    }

    private void generateConstantFile() {
        File configFile = new File(filePath);
        File configparentDir = configFile.getParentFile();
        if (configparentDir != null && !configparentDir.exists()) {
            if (!configparentDir.mkdirs()) {
                System.err.println("无法创建目录: " + configparentDir.getAbsolutePath());
                return;
            }
        }
        if (configFile.exists()) {
            //System.out.println("文件已存在，将删除: " + filePath);
            if (!configFile.delete()) {
                //System.err.println("无法删除现有文件: " + filePath);
                return;
            }
            System.out.println("已删除现有文件: " + filePath);
        }

        String contentpro = String.format(
                "diverClassName = com.mysql.jdbc.Driver\n" +
                        "url = jdbc:mysql://%s:%d/%s?useSSL=false&useServerPrepStmts=true\n" +
                        "username = %s\n" +
                        "password = %s\n" +
                        "initialSize = 5\n" +
                        "maxActive = 10\n" +
                        "maxWait = 30000\n",
                dbAddress, dbPort, dbName, dbUsername, dbPassword
        );
        //System.out.println("生成文件内容："+contentpro);
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(contentpro);
            //System.out.println("文件创建成功: " + filePath);
        } catch (IOException e) {
            System.err.println("创建文件时发生错误: " + e.getMessage());
        }

        File file = new File(additionalPath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("无法创建目录: " + parentDir.getAbsolutePath());
                return;
            }
        }

        if (file.exists()) {
            System.out.println("文件已存在，将删除: " + filePath);
            if (!file.delete()) {
                System.err.println("无法删除现有文件: " + filePath);
                return;
            }
            System.out.println("已删除现有文件: " + filePath);
        }

        String content = String.format(
                "public class constant {\n" +
                "    public static final int PORT = %d;\n" +
                "    public static final String FILE_PATH = \"%s\";\n" +
                "}\n",
                serverPort, filePath
        );
        System.out.println(filePath);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            System.out.println("文件创建成功: " + filePath);
        } catch (IOException e) {
            System.err.println("创建文件时发生错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerControlPanel frame = new ServerControlPanel();
            frame.setVisible(true);
        });
    }
}