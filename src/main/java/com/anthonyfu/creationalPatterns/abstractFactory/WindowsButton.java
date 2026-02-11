package com.anthonyfu.creationalPatterns.abstractFactory;

class WindowsButton implements Button {
    @Override
    public void click() {
        System.out.println("Click Windows Button");
    }
}