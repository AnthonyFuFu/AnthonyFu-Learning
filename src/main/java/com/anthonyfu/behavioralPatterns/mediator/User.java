package com.anthonyfu.behavioralPatterns.mediator;

//用戶抽象類
abstract class User {
	protected ChatMediator mediator;
	protected String name;

	public User(ChatMediator mediator, String name) {
		this.mediator = mediator;
		this.name = name;
	}

	public abstract void send(String message);

	public abstract void receive(String message);
}
