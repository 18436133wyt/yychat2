package com.yychat.model;

public interface MessageType {
	public static final String message_LoginFailure="0";//字符串常量
	String message_LoginSuccess="1";
	String message_Common="2";	
	String message_RequestOnlineFriend="3";
	String message_OnlineFriend="4";
	/*String message_finishedEabelonlinefriend="5";
	String message_newfriendupdate="6";*/
	String message_NewOnlineFriend="5";
	String message_RegisterSuccess="6";
	String message_RegisterFailure="7";
	//添加新好友，步骤3、增加消息类型
	String message_AddFriend="8";
	String message_AddFriendSuccess="9";
	String message_AddFriendFailure_NoUser="10";
	String message_AddFriendFailure_AlreadyFriend="11";
}
