package com.AnthonyFuLearning.decoratorPattern;

public class Client {
    public static void main(String[] args) {
        // 點一杯Latte，添加Milk和Sugar、Syrup
        Coffee latte = new Milk(new Sugar (new Syrup(new Latte())));
        System.out.println("Cost: " + latte.getCost() + ", Description: " + latte.getDescription());
    }
}