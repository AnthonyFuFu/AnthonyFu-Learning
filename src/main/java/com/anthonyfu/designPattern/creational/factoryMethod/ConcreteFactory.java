package com.anthonyfu.designPattern.creational.factoryMethod;

public class ConcreteFactory extends Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProduct();
    }
}