package com.AnthonyFuLearning.creationalPatterns.abstractFactory;

class MacButton implements Button {
    @Override
    public void click() {
        System.out.println("Click Mac Button");
    }
}
