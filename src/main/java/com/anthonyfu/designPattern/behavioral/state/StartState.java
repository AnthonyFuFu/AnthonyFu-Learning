package com.anthonyfu.designPattern.behavioral.state;

//具體狀態 - 開始狀態
class StartState implements State {
	@Override
	public void handle(Context context) {
		System.out.println("Player is in start state");
		context.setState(new RunningState());
	}

	@Override
	public String getStateName() {
		return "Start State";
	}
}
