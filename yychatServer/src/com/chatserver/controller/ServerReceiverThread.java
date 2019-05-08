package com.chatserver.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import com.yychat.model.Message;

public class ServerReceiverThread extends Thread{//必须要有run()方法
	Socket s;
	HashMap hmSocket;
	String sender;
	Message mess;
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
				
				//转发Message信息
				if(mess.getMessageType().equals(Message.message_Common)){
					Socket s1=(Socket)hmSocket.get(mess.getReceiver());
					ObjectOutputStream oos=new ObjectOutputStream(s1.getOutputStream());
					oos.writeObject(mess);//转发Message
					System.out.println("服务器转发了信息"+mess.getSender()+"对"+mess.getReceiver()+"说:"+mess.getContent());
				}
				if(mess.getMessageType().equals(Message.message_RequestOnlineFriend)){
					
					Set friendSet=StartServer.hmSocket.keySet();
					Iterator it=friendSet.iterator();
					String friendName;
					String friendString="";
					while(it.hasNext()){
						friendName=(String)it.next();
						if(!friendName.equals(mess.getSender()));
						friendString=friendName+" "+friendString;
					}
					System.out.println("全部好友的名字"+friendString);
					
					//给mess赋值
					mess.setContent(friendString);
					mess.setReceiver(sender);
					mess.setSender("Server");
					mess.setMessageType(Message.message_OnlineFriend);
				}
				
				//发送mess
				Socket s1=(Socket)hmSocket.get(sender);
				sendMessage(s1,mess);
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
			
		
		
		
	}

	private void sendMessage(Socket s, Message mess) throws IOException{
		oos=new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(mess);
		
	}
	
}
