package com.AnthonyFuLearning.behavioralPatterns.command;

//命令接口
interface Command {
	void execute();
	void undo();
}