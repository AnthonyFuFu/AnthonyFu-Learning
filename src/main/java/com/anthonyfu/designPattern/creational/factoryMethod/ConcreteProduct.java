package com.anthonyfu.designPattern.creational.factoryMethod;

public class ConcreteProduct extends Product {
    @Override
    public void doSomething() {
        System.out.println("ConcreteProduct do something");
    }
}