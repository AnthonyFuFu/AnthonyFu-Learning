package com.AnthonyFuLearning.decoratorPattern;

public class Latte implements Coffee {
    @Override
    public double getCost() {
        return 2.99;
    }

    @Override
    public String getDescription() {
        return "Latte";
    }
}
