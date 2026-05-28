package com.anthonyfu.designPattern.behavioral.mediator;

import java.util.ArrayList;
import java.util.List;

//具體中介者
class ChatRoom implements ChatMediator {
	private List<User> users;

	public ChatRoom() {
		this.users = new ArrayList<>();
	}

	@Override
	public void sendMessage(String message, User user) {
		// 將消息發送給除發送者以外的所有用戶
		for (User u : users) {
			if (u != user) {
				u.receive(message);
			}
		}
	}

	@Override
	public void addUser(User user) {
		users.add(user);
	}
}
