package com.anthonyfu.designPattern.structural.decorator;

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", with Sugar";
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.2;
    }
}
