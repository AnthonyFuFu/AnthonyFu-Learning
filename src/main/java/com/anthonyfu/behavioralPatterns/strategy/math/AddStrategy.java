package com.anthonyfu.behavioralPatterns.strategy.math;

public class AddStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        return a + b;
    }
}