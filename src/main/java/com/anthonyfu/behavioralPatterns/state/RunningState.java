package com.anthonyfu.behavioralPatterns.state;

//具體狀態 - 運行狀態
class RunningState implements State {
	@Override
	public void handle(Context context) {
		System.out.println("Player is running");
		context.setState(new StopState());
	}

	@Override
	public String getStateName() {
		return "Running State";
	}
}