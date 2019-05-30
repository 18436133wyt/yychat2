package com.chatserver.controller;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.yychat.model.Message;
import com.yychat.model.User;

public class StartServer {
	ServerSocket ss;
	Socket s;
	String passWord;
	Message mess;
	String userName;
	
	public static HashMap hmSocket=new HashMap<String,Socket>();//泛型，通用类
	public StartServer(){
		try {
			ss=new ServerSocket(3456);//服务器端口监听3456
			System.out.println("服务器已经启动，监听3456端口...");
			while(true){//?多线程问题
				s=ss.accept();//等待客户端建立连接
				System.out.println(s);//输出连接Socket对象
				//字节输入流 包装成 对象输入流
				ObjectInputStream ois=new ObjectInputStream(s.getInputStream());
				User user=(User)ois.readObject();//接收用户登录对象user
				this.userName=user.getUserName();
				this.passWord=user.getPassWord();
				System.out.println(user.getUserName());
				System.out.println(user.getPassWord());
				//注册新用户步骤7：在服务器完成新用户的注册
				if(user.getUserMessageType().equals("USER_REGISTER")){
					//注册新用户步骤8：对注册用户名进行查询,seekSuccess为true,存在同名用户；为false，不存在同名用户
					boolean seekSuccess=YychatDbUtil.seekUser(userName);
					mess=new Message();
    				mess.setSender("Server");
    				mess.setReceiver(userName);
					if(seekSuccess){
						//返回注册失败的message
	    				mess.setMessageType(Message.message_RegisterFailtrue);
					}else{
						//注册新用户步骤9：如果没有同名用户，把新用户的名字和密码写入到user表中
						YychatDbUtil.addUser(userName,passWord);
						mess.setMessageType(Message.message_RegisterSuccess);
					}
					sendMessage(s,mess);
					s.close();//注册结束，应该关闭服务器的socket对象
				}
				
                if(user.getUserMessageType().equals("USER_LOGIN")){
                	//使用数据库来验证用户名和密码
    				boolean loginSuccess=YychatDbUtil.looginValidate(userName,passWord);
    				System.out.println(loginSuccess);
    				//Server端验证密码是否“123456”
    				Message mess=new Message();
    				mess.setSender("Server");
    				mess.setReceiver(user.getUserName());
    				if(loginSuccess){//不能用"==",对象比较
    					//消息传递，创建一个Message对象				
    					mess.setMessageType(Message.message_LoginSuccess);//验证通过		
    					//从数据库 relation表中读取好友信息来更新好友列表1.服务器读好友数据出来
    					String friendString=YychatDbUtil.getFriendString(userName);
    					mess.setContent(friendString);//保存好友信息到mess对象的content成员
    					System.out.println(userName+"的全部好友："+friendString);
    					}
    				else{				
    					mess.setMessageType(Message.message_LoginFailure);//验证不通过	
    				}				
    				ObjectOutputStream oos=new ObjectOutputStream(s.getOutputStream());
    				oos.writeObject(mess);
    				if(loginSuccess){
    					mess.setMessageType(Message.message_NewOnlineFriend);
    					mess.setSender("Server");
    					mess.setContent(this.userName);
    					Set friendSet=hmSocket.keySet();
    					Iterator it=friendSet.iterator();
    					String friendName;
    					while(it.hasNext()){
    						friendName=(String)it.next();
    						mess.setReceiver(friendName);
    						Socket s1=(Socket)hmSocket.get(friendName);
    						oos=new ObjectOutputStream(s1.getOutputStream());
    						oos.writeObject(mess);
    					}
    					//保存每一个用户对应的Socket
    					hmSocket.put(userName,s);
    					System.out.println("保存用户的Socket"+userName+s);
    					//如何接收客户端的聊天信息？另建一个线程来接收聊天信息
    					new ServerReceiverThread(s,hmSocket).start();//创建线程，并让线程就绪
    					System.out.println("启动线程成功");
    				}
    				
    			}
				}
							
			
		} catch (IOException | ClassNotFoundException e) {			
			e.printStackTrace();
		}
	}
	private void sendMessage(Socket s2, Message mess2) {
		ObjectOutputStream oos1;
		try {
			oos1 = new ObjectOutputStream(s2.getOutputStream());
			oos1.writeObject(mess2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
