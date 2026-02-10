package com.AnthonyFuLearning.behavioralPatterns.command;

//無操作的命令 (空對象模式)
class NoCommand implements Command {
    @Override
    public void execute() {}

    @Override
    public void undo() {}
}