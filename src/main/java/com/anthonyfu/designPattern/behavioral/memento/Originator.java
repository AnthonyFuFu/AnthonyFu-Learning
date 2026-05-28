package com.anthonyfu.designPattern.behavioral.memento;

//原發器 - 需要保存狀態的對象
class Originator {
	private String state;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
		System.out.println("State changed to: " + state);
	}

	// 創建備忘錄
	public Memento saveStateToMemento() {
		return new Memento(state);
	}

	// 從備忘錄恢復狀態
	public void restoreStateFromMemento(Memento memento) {
		state = memento.getState();
		System.out.println("State restored to: " + state);
	}
}