package com.AnthonyFuLearning.behavioralPatterns.state;

//具體狀態 - 停止狀態
class StopState implements State {
	@Override
	public void handle(Context context) {
		System.out.println("Player is stopped");
		context.setState(new StartState());
	}

	@Override
	public String getStateName() {
		return "Stop State";
	}
}
