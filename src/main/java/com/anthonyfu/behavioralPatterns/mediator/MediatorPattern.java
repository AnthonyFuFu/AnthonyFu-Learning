package com.anthonyfu.behavioralPatterns.mediator;

public class MediatorPattern {
	public static void main(String[] args) {
		// 創建中介者 - 聊天室
		ChatMediator chatRoom = new ChatRoom();

		// 創建用戶
		User alice = new ChatUser(chatRoom, "Alice");
		User bob = new ChatUser(chatRoom, "Bob");
		User charlie = new ChatUser(chatRoom, "Charlie");

		// 將用戶添加到聊天室
		chatRoom.addUser(alice);
		chatRoom.addUser(bob);
		chatRoom.addUser(charlie);

		// 用戶發送消息
		alice.send("Hi everyone!");
		bob.send("Hey Alice!");
		charlie.send("Hello there!");
	}
}
