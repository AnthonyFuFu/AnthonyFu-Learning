package com.anthonyfu.behavioralPatterns.memento;

//備忘錄 - 保存狀態
class Memento {
	private String state;

	public Memento(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}
}
