package com.anthonyfu.designPattern.behavioral.command;

//命令接口
interface Command {
	void execute();
	void undo();
}