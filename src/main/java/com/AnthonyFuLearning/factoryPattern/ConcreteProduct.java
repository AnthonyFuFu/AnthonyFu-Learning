package com.AnthonyFuLearning.factoryPattern;

public class ConcreteProduct extends Product {
    @Override
    public void doSomething() {
        System.out.println("ConcreteProduct do something");
    }
}