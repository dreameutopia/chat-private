package cn.dreameutopia.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ChatHistoryWindow extends JFrame {
    private JTextArea chatHistoryArea;
    private JTextField searchField;
    private JButton searchByNameButton;
    private JButton searchByTimeButton;
    private JButton searchByContentButton;

    private ChatWindow chatWindow;

    public ChatHistoryWindow(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
        initialize();
    }

    private void initialize() {
        chatWindow.submmitquery();
        setTitle("聊天记录");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 聊天记录显示区域
        chatHistoryArea = new JTextArea();
        chatHistoryArea.setEditable(false);
        chatHistoryArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        chatHistoryArea.setLineWrap(true);
        chatHistoryArea.setWrapStyleWord(true);
        JScrollPane chatHistoryScrollPane = new JScrollPane(chatHistoryArea);

        // 搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());

        searchField = new JTextField(20);
        searchByNameButton = new JButton("按用户名搜索");
        searchByTimeButton = new JButton("按时间搜索");
        searchByContentButton = new JButton("按内容搜索");

        searchPanel.add(searchField);
        searchPanel.add(searchByNameButton);
        searchPanel.add(searchByTimeButton);
        searchPanel.add(searchByContentButton);

        // 按钮事件处理
        searchByNameButton.addActionListener(e -> searchChatHistory("name"));
        searchByTimeButton.addActionListener(e -> searchChatHistory("time"));
        searchByContentButton.addActionListener(e -> searchChatHistory("content"));

        add(chatHistoryScrollPane, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        setLocationRelativeTo(chatWindow);

    }


    private void searchChatHistory(String type) {

        String query = searchField.getText();
        if(query.equals("")){
            return;
        }
        Map<String,String> messageMap=chatWindow.getChatMap();
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String,String>s1:messageMap.entrySet()){
            String date=s1.getKey();
            String data=s1.getValue();
            String[] data1=data.split(",");
            String username=data1[0];
            String content=data1[1];
            if (type.equals("name") && username.contains(query)) {
                //System.out.println("搜索到用户名："+username);
                result.append(username).append(" ").append(date).append("\r\n").append(" ").append(content).append("\n");
            } else if (type.equals("time") && date.contains(query)) {
                //System.out.println("搜索到时间："+date);
                result.append(username).append(" ").append(date).append("\r\n").append(" ").append(content).append("\n");
            } else if (type.equals("content") && content.contains(query)) {
                //System.out.println("搜索到内容："+content);
                result.append(username).append(" ").append(date).append("\r\n").append(" ").append(content).append("\n");
            }

        }

        chatHistoryArea.setText(result.toString());

    }
}