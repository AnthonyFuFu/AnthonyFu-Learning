package com.anthonyfu.behavioralPatterns.templateMethod;

//具體實現類 - 籃球遊戲
class Basketball extends Game {
	@Override
	protected void initialize() {
		System.out.println("Basketball Game Initialized! Start playing.");
	}

	@Override
	protected void startPlay() {
		System.out.println("Basketball Game Started. Enjoy the game!");
	}

	@Override
	protected void endPlay() {
		System.out.println("Basketball Game Finished!");
	}
}
