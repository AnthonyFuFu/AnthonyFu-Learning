package com.AnthonyFuLearning.creationalPatterns.abstractFactory;

class MacCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Check Mac Checkbox");
    }
}
