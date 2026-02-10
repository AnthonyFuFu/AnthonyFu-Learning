package com.AnthonyFuLearning.creationalPatterns.factoryMethod;

public class ConcreteProduct extends Product {
    @Override
    public void doSomething() {
        System.out.println("ConcreteProduct do something");
    }
}