package com.AnthonyFuLearning.creationalPatterns.factoryMethod;

public class CarFactory extends Factory {
    @Override
    public Product createProduct() {
        return new CarProduct();
    }
}