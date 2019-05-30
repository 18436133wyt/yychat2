package com.yychat.model;

import java.io.Serializable;

public class User implements Serializable{
	private String userName;//成员变量
	private String passWord;
	//注册新用户的步骤3：在User类中添加新的成员变量"USER_REGISTER"和"USER_LOGIN"
		private String useMessageType;
		
		public String getUserMessageType(){
			return useMessageType;
		}
		public void setUserMessageType(String useMessageType){
			this.useMessageType=useMessageType;
		}
		
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;//局部变量给成员变量赋值
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	
	
}
