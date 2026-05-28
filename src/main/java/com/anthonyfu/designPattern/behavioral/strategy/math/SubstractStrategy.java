package com.anthonyfu.designPattern.behavioral.strategy.math;

public class SubstractStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        return a - b;
    }
}