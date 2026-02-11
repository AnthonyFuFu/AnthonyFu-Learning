package com.anthonyfu.structuralPatterns.decorator;

public class Sugar extends CoffeeDecorator {
    public Sugar(Coffee decoratedCoffee) {
        super(decoratedCoffee);
    }
    
    public double getCost() {
        return super.getCost() + 0.2;
    }
    
    public String getDescription() {
        return super.getDescription() + ", Sugar";
    }
}