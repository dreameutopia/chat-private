## 使用教程
### 服务端
1. 打开ServerControlPanel，填入数据，第一个和最后两个可以不填，为初始默认存放位置，其他数据可以自行修改，默认打开本地数据库3306端口，itcast数据库，基于druid线程池


   ![image](https://github.com/user-attachments/assets/270b2d65-c57a-4e62-acc9-4459ee907284)


2. 运行cn.dreameutopia.server包下server即可

### 客户端
1. 打开configure，填入数据


   ![image](https://github.com/user-attachments/assets/b4896327-b21c-472c-bd14-1e6bde2d08c8)



   
3. 运行cn.dreameutopia.ui.ChatEntryForm包下的app即可

## 界面展示
1. 登录界面



   ![image](https://github.com/user-attachments/assets/18dd7d90-1b14-4560-9a84-9a3bf397ee13)

   

2. 注册界面

   
   ![image](https://github.com/user-attachments/assets/4f2c3bd3-ce4b-45a4-b7ba-ba864f4db34d)


   

3. 修改密码


   ![image](https://github.com/user-attachments/assets/a5f83f9a-bb3c-45a6-800a-bddddb08bc3a)

   
   
4. 群聊界面


   ![image](https://github.com/user-attachments/assets/8e666599-c8ab-4444-97ba-026d09596c24)

   

5. 私聊界面


   ![image](https://github.com/user-attachments/assets/2a4e6983-5ffd-4805-aca6-a8112e7d6182)

   

6. 完善个人信息界面


   ![image](https://github.com/user-attachments/assets/449cef01-d3e9-47d0-9551-8230fdd96b52)

   

7. 查看对方信息界面


  ![image](https://github.com/user-attachments/assets/35e376bc-59a2-4a6c-a13e-a2cfc5331c66)


   

## 项目亮点
1. 可扩展性强，各类分工明确
2. 基于多线程，可以支持多发多收
3. 依靠数据库，默认保存所有历史记录，查询时自动缓存进内存
4. 支持修改密码，完善和查看个人信息，可以随意添加
5. 服务端和客户端都有配置面板，编译前可自行调整ip和端口

## 后续改进
1. 接入邮箱api
2. 接入背景自定义
3. 支持收发文件
