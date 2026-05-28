package com.anthonyfu.designPattern.behavioral.state;

interface State {
    void handle(Context context);
    String getStateName();
}
