package com.anthonyfu.behavioralPatterns.templateMethod;

//具體實現類 - 足球遊戲
class Football extends Game {
	@Override
	protected void initialize() {
		System.out.println("Football Game Initialized! Start playing.");
	}

	@Override
	protected void startPlay() {
		System.out.println("Football Game Started. Enjoy the game!");
	}

	@Override
	protected void endPlay() {
		System.out.println("Football Game Finished!");
	}
}