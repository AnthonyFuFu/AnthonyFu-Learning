package com.AnthonyFuLearning.behavioralPatterns.chainOfResponsibility;

class Level2Handler extends Handler {
    @Override
    public void handleRequest(int request) {
        if (request <= 20) {
            System.out.println("Level 2 Handler handled request: " + request);
        } else if (successor != null) {
            successor.handleRequest(request);
        }
    }
}