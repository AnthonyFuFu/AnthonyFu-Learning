package com.anthonyfu.designPattern.creational.abstractFactory;

class WindowsCheckbox implements Checkbox {
    @Override
    public void check() {
        System.out.println("Check Windows Checkbox");
    }
}
