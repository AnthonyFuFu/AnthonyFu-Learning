package com.anthonyfu.designPattern.behavioral.command;

public class CommandPattern {
    public static void main(String[] args) {
        // 創建接收者
        Light livingRoomLight = new Light("Living Room");
        Light kitchenLight = new Light("Kitchen");

        // 創建命令
        Command livingRoomLightOn = new LightOnCommand(livingRoomLight);
        Command livingRoomLightOff = new LightOffCommand(livingRoomLight);
        Command kitchenLightOn = new LightOnCommand(kitchenLight);
        Command kitchenLightOff = new LightOffCommand(kitchenLight);

        // 創建調用者
        RemoteControl remote = new RemoteControl();
        
        // 設置命令
        remote.setCommand(0, livingRoomLightOn, livingRoomLightOff);
        remote.setCommand(1, kitchenLightOn, kitchenLightOff);

        // 使用遙控器按鈕
        remote.onButtonPressed(0);  // 開啟客廳燈
        remote.offButtonPressed(0); // 關閉客廳燈
        remote.undoButtonPressed(); // 撤銷 - 再次開啟客廳燈
        
        remote.onButtonPressed(1);  // 開啟廚房燈
        remote.offButtonPressed(1); // 關閉廚房燈
    }
}
