package com.chatserver.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import com.yychat.model.Message;

public class ServerReceiverThread extends Thread{//必须要有run()方法
	Socket s;
	HashMap hmSocket;
	Message mess;
	String sender;
	String receiver;
	ObjectOutputStream oos;
	 
	public ServerReceiverThread(Socket s,HashMap hmSocket){
		this.s=s;
		this.hmSocket=hmSocket;		
	}
	
	public void run(){		
		ObjectInputStream ois;
		while(true){
			try {
				//接收Message信息
				ois=new ObjectInputStream(s.getInputStream());
				mess=(Message)ois.readObject();//接收用户发送来的聊天信息，阻塞，10个用户，100毫秒
				System.out.println("等待用户发送聊天信息");
				System.out.println(mess.getSender()+"对"+mess.getReceiver()+"说:"+mess.getContent());
				sender=mess.getSender();
				receiver=mess.getReceiver();
				
				//添加新的好友，步骤5、处理添加好友的信息
				if(mess.getMessageType().equals(Message.message_AddFriend)){
					String addFriendName=mess.getContent();
					System.out.println("需要添加的好友的名字为："+addFriendName);
					//步骤6、在User表中查找addFriendName
					if(!YychatDbUtil.seekUser(addFriendName)){
						//不存在，不能添加好友
						mess.setMessageType(Message.message_AddFriendFailure_NoUser);
					}else{
						//步骤7、用户存在,查寻relation表中，看该用户是不是好友
						String friendRelation="1";
						if(YychatDbUtil.seekRelation(sender,addFriendName,friendRelation)){//查询friendRelation关系
							//是好友关系
							mess.setMessageType(Message.message_AddFriendFailure_AlreadyFriend);
						}else{
							//步骤8、不是好友关系，此时才能添加好友
							int count=YychatDbUtil.addRelation(sender,addFriendName,friendRelation);
							if(count!=0){
								mess.setMessageType(Message.message_AddFriendSuccess);
								String allFriendName=YychatDbUtil.getFriendString(sender);
								System.out.println("服务器拿到的全部好友名字："+allFriendName);
								mess.setContent(allFriendName);
							}							
						}						
					}
					sendMessage(s, mess);				
				}
				
				//保存聊天信息实验之步骤2、转发聊天信息，并且保存到message表中
				if(mess.getMessageType().equals(Message.message_Common)){
					String content =mess.getContent();//需要保存的聊天信息
					YychatDbUtil.addMessage(sender,receiver,content);//添加信息到message表中的方法
					mess.setSendTime(new Date());
					Socket s1=(Socket)hmSocket.get(mess.getReceiver());
					sendMessage(s1,mess);
					System.out.println("服务器转发了信息"+mess.getSender()+"对"+mess.getReceiver()+"说:"+mess.getContent());
				}
				
				//第2步骤，返回在线好友信息到客户端
				if(mess.getMessageType().equals(Message.message_RequestOnlineFriend)){
					//首先要拿到在线好友信息
					Set friendSet=StartServer.hmSocket.keySet();//得到好友名字集合
					Iterator it=friendSet.iterator();//迭代器，目的是对friendSet集合中的元素进行遍历
					String friendName;
					String friendString=" ";
					while(it.hasNext()){//判断还有没有下一个好友
						friendName=(String)it.next();//获取下一个好友名字
						if(!friendName.equals(mess.getSender()))//排除自己
							System.out.println("有执行该代码！");
						     friendString=friendString+friendName+" ";//为什么要加空格？
					}
					System.out.println("全部好友的名字："+friendString);
					
					//给mess赋值
					mess.setContent(friendString);
					mess.setReceiver(sender);
					mess.setSender("Server");
					mess.setMessageType(Message.message_OnlineFriend);
					
					//发送mess
					Socket s1=(Socket)hmSocket.get(sender);
					sendMessage(s1,mess);
					
				}
				/*if(mess.getMessageType().equals(Message.message_finishedEabelonlinefriend)){
					Set friendSet=StartServer.hmSocket.keySet();//得到好友名字集合
					Iterator it=friendSet.iterator();//迭代器，目的是对friendSet集合中的元素进行遍历
					String friendName;
					mess.setMessageType(Message.message_newfriendupdate);
					while(it.hasNext()){//判断还有没有下一个好友
						friendName=(String)it.next();//获取下一个好友名字
						if(!friendName.equals(mess.getSender())){
							Socket s1=(Socket)hmSocket.get(friendName);//获取每一个用户的Socket
							mess.setReceiver(friendName);
							ObjectOutputStream oos1;
							oos=new ObjectOutputStream(s1.getOutputStream());
							oos.writeObject(mess);
						}//排除自己
							System.out.println("有执行该代码！");
					}
				}*/
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
			
		
		
		
	}

	public void sendMessage(Socket s,Message mess) throws IOException {
		oos=new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(mess);//转发Message
	}
	
}
