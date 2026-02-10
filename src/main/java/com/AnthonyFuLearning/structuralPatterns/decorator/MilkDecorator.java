package com.AnthonyFuLearning.structuralPatterns.decorator;

class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", with Milk";
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.5;
    }
}