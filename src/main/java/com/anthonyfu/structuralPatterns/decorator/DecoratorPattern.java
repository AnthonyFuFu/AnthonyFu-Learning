package com.anthonyfu.structuralPatterns.decorator;

public class DecoratorPattern {
    public static void main(String[] args) {
    	// 裝飾器模式：用於需要動態地添加功能，而且這些功能可以組合使用的情況，如 Java IO 類庫。
    	// 裝飾器模式允許動態地給對象添加新功能，不改變其結構。
        // 點一杯Latte，添加Milk和Sugar、Syrup
        Coffee latte = new Milk(new Sugar (new Syrup(new Latte())));
        System.out.println("Cost: " + latte.getCost() + ", Description: " + latte.getDescription());
        
        // 創建一個簡單咖啡
        Coffee simpleCoffee = new SimpleCoffee();
        System.out.println("Cost: $" + simpleCoffee.getCost() + ", Description: " + simpleCoffee.getDescription());
        
        // 添加牛奶
        Coffee milkCoffee = new MilkDecorator(simpleCoffee);
        System.out.println("Cost: $" + milkCoffee.getCost() + ", Description: " + milkCoffee.getDescription());
        
        // 添加糖
        Coffee sweetMilkCoffee = new SugarDecorator(milkCoffee);
        System.out.println("Cost: $" + sweetMilkCoffee.getCost() + ", Description: " + sweetMilkCoffee.getDescription());
    }
}