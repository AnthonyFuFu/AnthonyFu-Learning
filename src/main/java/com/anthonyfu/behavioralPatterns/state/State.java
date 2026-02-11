package com.anthonyfu.behavioralPatterns.state;

interface State {
    void handle(Context context);
    String getStateName();
}
