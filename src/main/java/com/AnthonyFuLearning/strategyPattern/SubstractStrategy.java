package com.AnthonyFuLearning.strategyPattern;

public class SubstractStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        return a - b;
    }
}