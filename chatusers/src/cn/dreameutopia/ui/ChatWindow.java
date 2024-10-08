package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class ChatWindow extends JFrame {
    private JTextArea groupMessageArea;
    private JTextField groupMessageField;
    private JButton groupSendButton;
    private String nickname;
    private JList<String> userList; // 在线人员列表
    private DefaultListModel<String> listModel;
    private Socket socket;

    private Map<String, PrivateChatPanel> privateChatPanels;
    private CardLayout cardLayout; // 卡片布局
    private JPanel cardPanel;
    private JPanel groupChatPanel;

    private JLabel privateMessageLabel; // 新增的标签
    private  Map<String,String> messageMap=new LinkedHashMap<>();

    public boolean initcomplete=false;
    List<String> userinfo;
    public boolean inituserinfo=false;

    public ChatWindow(String nickname, Socket socket) {
        this.nickname = nickname;
        this.socket = socket;
        initialize();

        // 立即把客户端的这个socket管道交给一个独立线程专门负责读取客户端socket从服务端收到的在线人数更新数据或者群聊数据
        new clientReader(socket, this).start();
    }

    private void initialize() {
        setTitle("公共局域网聊天 您的名称： " + nickname);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // 卡片布局
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 初始化群聊界面
        initializeGroupChatPanel();

        // 初始化私聊界面的Map
        privateChatPanels = new HashMap<>();

        cardPanel.add(groupChatPanel, "groupChat");

        // 新增的标签
        privateMessageLabel = new JLabel("无新私聊消息", SwingConstants.CENTER);
        privateMessageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        privateMessageLabel.setForeground(Color.BLUE);
        add(privateMessageLabel, BorderLayout.NORTH);

        add(cardPanel, BorderLayout.CENTER);

        // 在线人员列表
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        JScrollPane userScrollPane = new JScrollPane(userList);

        // 添加鼠标监听器，点击用户名时跳转到私聊界面
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = userList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String selectedUser = listModel.getElementAt(index);
                        switchToPrivateChat(selectedUser);
                    }
                }
            }
        });

        // 分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardPanel, userScrollPane);
        splitPane.setDividerLocation(600); // 设置分割线位置
        add(splitPane, BorderLayout.CENTER);

        setLocationRelativeTo(null); // 居中显示
    }

    private void initializeGroupChatPanel() {
        groupChatPanel = new JPanel(new BorderLayout());

        // 群聊消息显示区域
        groupMessageArea = new JTextArea();
        groupMessageArea.setEditable(false);
        groupMessageArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        groupMessageArea.setLineWrap(true);
        groupMessageArea.setWrapStyleWord(true);
        JScrollPane groupMessageScrollPane = new JScrollPane(groupMessageArea);

        // 输入面板
        JPanel groupInputPanel = new JPanel();
        groupInputPanel.setBackground(Color.WHITE);
        groupInputPanel.setLayout(new BorderLayout());

        groupMessageField = new JTextField();
        groupMessageField.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        groupInputPanel.add(groupMessageField, BorderLayout.CENTER);

        // 添加查看聊天记录按钮
        JButton viewHistoryButton = new JButton("查看聊天记录");
        viewHistoryButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        viewHistoryButton.setBackground(new Color(0x4CAF50));
        viewHistoryButton.setForeground(Color.WHITE);
        viewHistoryButton.setFocusPainted(false);
        viewHistoryButton.addActionListener(e -> openChatHistoryWindow());

        groupSendButton = new JButton("发送");
        groupSendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        groupSendButton.setBackground(new Color(0x4CAF50));
        groupSendButton.setForeground(Color.WHITE);
        groupSendButton.setFocusPainted(false);

        // 这里交换了按钮的位置
        groupInputPanel.add(viewHistoryButton, BorderLayout.WEST);
        groupInputPanel.add(groupSendButton, BorderLayout.EAST);

        groupChatPanel.add(groupMessageScrollPane, BorderLayout.CENTER);
        groupChatPanel.add(groupInputPanel, BorderLayout.SOUTH);

        // 按钮监听器
        groupSendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = groupMessageField.getText();
                groupMessageField.setText("");
                // 发送消息
                sendMessageToServer(2, message);
            }
        });
    }

    private void openChatHistoryWindow() {
        ChatHistoryWindow chatHistoryWindow = new ChatHistoryWindow(this);
        chatHistoryWindow.setVisible(true);
    }

    private void switchToPrivateChat(String user) {
        // 如果点击的用户就是自己，弹出提示框“您不可以和自己对话”，并返回
        if (user.equals(nickname)) {
            JOptionPane.showMessageDialog(this, "您不可以和自己对话！");
            return;
        }

        if (!privateChatPanels.containsKey(user)) {
            // 如果不存在该用户的私聊窗口，则创建一个新的
            PrivateChatPanel privateChatPanel = new PrivateChatPanel(socket, this);
            // 设置当前私聊用户
            privateChatPanel.setCurrentPrivateChatUser(user);
            privateChatPanel.setName(user); // 设置面板名称
            setTitle("与" + user + "私聊界面 ");

            privateChatPanels.put(user, privateChatPanel); // 添加到Map中
            cardPanel.add(privateChatPanel, user);
        }

        // 切换到私聊界面
        cardLayout.show(cardPanel, user);
    }

    public void switchToGroupChat() {
        cardLayout.show(cardPanel, "groupChat");
        setTitle("公共局域网聊天 您的名称： " + nickname);
    }

    private void sendMessageToServer(int type, String message) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(type);
            if (type == 3) {
                dos.writeUTF(getCurrentPrivateChatUser()); // 目标用户
            }
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String user) {
        listModel.addElement(user);
    }

    public void updateOnlineUsers(List<String> onlineList) {
        listModel.clear();
        for (String user : onlineList) {
            listModel.addElement(user);
        }
    }

    public void setMsgToWin(int type, String msg) {
        if (type == 2) {
            groupMessageArea.append(msg + "\n");
        } else if (type == 3) {
            System.out.println("接受到私聊信息");
            String[] parts = msg.split(" ", 2);
            String user = parts[0];
            System.out.println("通过信息截取到用户名为" + user);
            // 更新标签内容
            privateMessageLabel.setText("有来自 " + user + " 的私聊消息");

            // 弹出提示框 “有来自于（）的私聊消息”
            JOptionPane.showMessageDialog(this, "有来自于 " + user + " 的私聊消息");
            PrivateChatPanel privateChatPanel = privateChatPanels.get(user);
            if (privateChatPanel != null) {
                System.out.println("已经创建过面板");
                privateChatPanel.getPrivateMessageArea().append(msg + "\n");
            } else {
                // 创建一个和这个人的私聊面板，并添加到Map中，并发送信息
                PrivateChatPanel newPrivateChatPanel = new PrivateChatPanel(socket, this);
                newPrivateChatPanel.setCurrentPrivateChatUser(user);
                newPrivateChatPanel.setName(user);
                privateChatPanels.put(user, newPrivateChatPanel);
                cardPanel.add(newPrivateChatPanel, user);
                newPrivateChatPanel.getPrivateMessageArea().append(msg + "\n");
            }
        }
    }

    public String getCurrentPrivateChatUser() {
        Component currentComponent = getCurrentVisibleComponent(cardPanel);
        if (currentComponent instanceof PrivateChatPanel) {
            return ((PrivateChatPanel) currentComponent).getCurrentPrivateChatUser();
        }
        return null;
    }

    private Component getCurrentVisibleComponent(Container container) {
        for (Component component : container.getComponents()) {
            if (component.isVisible()) {
                return component;
            }
        }
        return null;
    }
    public void submmitquery(){
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(9);//发送查询聊天记录的请求
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<String> getChatHistory() {






        return groupMessageArea.getText().lines().toList();
    }
    public Map<String,String> getChatMap(){
        return this.messageMap;
    }

    public void sendquerytoserver(String username) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(10);//发送查询聊天记录的请求
            dos.writeUTF(username);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showuserinfo(String name, String gender, String hobby, String birthday, String bio) {
        userinfo=new ArrayList<>();
        userinfo.add(name);
        userinfo.add(gender);
        userinfo.add(hobby);
        userinfo.add(birthday);
        userinfo.add(bio);
        inituserinfo=true;
        System.out.println(name+" "+gender+" "+hobby+" "+birthday+" "+bio);
        System.out.println("全部插入list中");

    }
    public List<String> getUserinfo(){

        return userinfo;

    }

    public void claerUserinfo() {
        userinfo=null;
    }
}