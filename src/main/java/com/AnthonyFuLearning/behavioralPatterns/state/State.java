package com.AnthonyFuLearning.behavioralPatterns.state;

interface State {
    void handle(Context context);
    String getStateName();
}
