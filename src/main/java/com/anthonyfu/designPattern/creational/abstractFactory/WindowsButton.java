package com.anthonyfu.designPattern.creational.abstractFactory;

class WindowsButton implements Button {
    @Override
    public void click() {
        System.out.println("Click Windows Button");
    }
}