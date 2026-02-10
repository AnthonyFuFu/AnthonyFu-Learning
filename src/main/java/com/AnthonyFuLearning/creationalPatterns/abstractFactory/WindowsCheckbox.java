package com.AnthonyFuLearning.creationalPatterns.abstractFactory;

class WindowsCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Check Windows Checkbox");
    }
}
