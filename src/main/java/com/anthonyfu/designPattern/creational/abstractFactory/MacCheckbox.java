package com.anthonyfu.designPattern.creational.abstractFactory;

class MacCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Check Mac Checkbox");
    }
}
