package com.AnthonyFuLearning.creationalPatterns.factoryMethod;

public class ConcreteFactory extends Factory {
    @Override
    public Product createProduct() {
        return new ConcreteProduct();
    }
}