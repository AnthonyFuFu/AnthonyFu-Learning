package com.anthonyfu.designPattern.creational.factoryMethod;

public class CarFactory extends Factory {
    @Override
    public Product createProduct() {
        return new CarProduct();
    }
}