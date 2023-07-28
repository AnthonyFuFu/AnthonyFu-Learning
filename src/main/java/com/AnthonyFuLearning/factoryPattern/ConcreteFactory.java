package com.AnthonyFuLearning.factoryPattern;

public class ConcreteFactory extends Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProduct();
    }
}