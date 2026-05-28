package com.anthonyfu.designPattern.structural.decorator;

public class Syrup extends CoffeeDecorator {
    public Syrup(Coffee coffee) {
        super(coffee);
    }

    public double getCost() {
        return super.getCost() + 0.25;
    }

    public String getDescription() {
        return super.getDescription() + ", Syrup";
    }
}