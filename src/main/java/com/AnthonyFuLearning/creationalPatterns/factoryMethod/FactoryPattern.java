package com.AnthonyFuLearning.creationalPatterns.factoryMethod;

public class FactoryPattern {
    public static void main(String[] args) {
    	// 工廠方法模式用於定義一個接口，讓子類決定要創建的對象類型。
        Factory concreteFactory = new ConcreteFactory();
        Product concreteProduct = concreteFactory.createProduct();
        concreteProduct.doSomething();
        
        Factory carFactory = new CarFactory();
        Product carProduct = carFactory.createProduct();
        carProduct.doSomething();
        
    }
}