package com.AnthonyFuLearning.strategyPattern;

public class Client {
    public static void main(String[] args) {
    	
    	Context context = new Context(new AddStrategy());
    	int result = context.executeStrategy(10, 5);
    	System.out.println(result); // output: 15
    	
    	context.setStrategy(new SubstractStrategy());
    	result = context.executeStrategy(10, 5);
    	System.out.println(result); // output: 5
    	
    	context.setStrategy(new MultiplyStrategy());
    	result = context.executeStrategy(10, 5);
    	System.out.println(result); // output: 50
    }
    
}