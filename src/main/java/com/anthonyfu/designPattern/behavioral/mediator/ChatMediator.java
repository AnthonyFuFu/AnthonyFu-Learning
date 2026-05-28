package com.anthonyfu.designPattern.behavioral.mediator;

//中介者接口
interface ChatMediator {
	void sendMessage(String message, User user);
	void addUser(User user);
}
