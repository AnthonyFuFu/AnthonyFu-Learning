package com.AnthonyFuLearning.factoryPattern;

public class Client {
    public static void main(String[] args) {
    	
        Factory concreteFactory = new ConcreteFactory();
        Product concreteProduct = concreteFactory.createProduct();
        concreteProduct.doSomething();
        
        Factory carFactory = new CarFactory();
        Product carProduct = carFactory.createProduct();
        carProduct.doSomething();
        
    }
}