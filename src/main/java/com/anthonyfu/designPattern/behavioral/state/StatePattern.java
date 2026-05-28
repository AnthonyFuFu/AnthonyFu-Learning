package com.anthonyfu.designPattern.behavioral.state;

public class StatePattern {
    public static void main(String[] args) {
        Context context = new Context();
        
        // 第一次請求，從開始狀態到運行狀態
        System.out.println("Current state: " + context.getState().getStateName());
        context.request();
        
        // 第二次請求，從運行狀態到停止狀態
        System.out.println("Current state: " + context.getState().getStateName());
        context.request();
        
        // 第三次請求，從停止狀態回到開始狀態
        System.out.println("Current state: " + context.getState().getStateName());
        context.request();
        
        // 狀態再次回到運行
        System.out.println("Current state: " + context.getState().getStateName());
    }
}
