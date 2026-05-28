package com.anthonyfu.designPattern.behavioral.state;

//上下文 - 維護當前狀態
class Context {
	private State state;

	public Context() {
		this.state = new StartState();
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public void request() {
		state.handle(this);
	}
}
