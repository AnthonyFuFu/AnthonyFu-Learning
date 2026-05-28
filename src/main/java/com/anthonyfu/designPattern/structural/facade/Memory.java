package com.anthonyfu.designPattern.structural.facade;

class Memory {
    public void load(long position, String data) {
        System.out.println("Memory: Loading from position " + position + " data: " + data);
    }
}
