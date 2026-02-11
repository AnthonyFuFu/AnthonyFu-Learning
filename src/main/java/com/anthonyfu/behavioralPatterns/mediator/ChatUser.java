package com.anthonyfu.behavioralPatterns.mediator;

//具體用戶
class ChatUser extends User {
	public ChatUser(ChatMediator mediator, String name) {
		super(mediator, name);
	}

	@Override
	public void send(String message) {
		System.out.println(name + " sends: " + message);
		mediator.sendMessage(message, this);
	}

	@Override
	public void receive(String message) {
		System.out.println(name + " receives: " + message);
	}
}