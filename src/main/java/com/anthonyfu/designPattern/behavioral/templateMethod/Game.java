package com.anthonyfu.designPattern.behavioral.templateMethod;

abstract class Game {
	// 模板方法 - 定義了算法的骨架
	public final void play() {
		initialize();
		startPlay();
		endPlay();
	}

	// 以下是需要子類實現的步驟
	protected abstract void initialize();
	protected abstract void startPlay();
	protected abstract void endPlay();
}