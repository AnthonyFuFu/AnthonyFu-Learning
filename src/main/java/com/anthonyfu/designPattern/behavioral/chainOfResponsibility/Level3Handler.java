package com.anthonyfu.designPattern.behavioral.chainOfResponsibility;

class Level3Handler extends Handler {
    @Override
    public void handleRequest(int request) {
        if (request <= 30) {
            System.out.println("Level 3 Handler handled request: " + request);
        } else {
            System.out.println("Request " + request + " is out of range, no handler can process it.");
        }
    }
}