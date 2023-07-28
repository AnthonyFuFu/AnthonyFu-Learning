package com.AnthonyFuLearning.factoryPattern;

public class CarFactory extends Factory {
    @Override
    public Product createProduct() {
        return new CarProduct();
    }
}