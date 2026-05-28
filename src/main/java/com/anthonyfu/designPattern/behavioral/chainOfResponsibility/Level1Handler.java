package com.anthonyfu.designPattern.behavioral.chainOfResponsibility;

class Level1Handler extends Handler {
    @Override
    public void handleRequest(int request) {
        if (request <= 10) {
            System.out.println("Level 1 Handler handled request: " + request);
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}