package com.anthonyfu.designPattern.creational.singleton;

public class Singleton {
    // 私有靜態實例
    private static Singleton instance;

    // 私有構造器，防止外部實例化
    private Singleton() {}

    // 提供全局訪問點 (懶加載)
    public static Singleton getInstance() {
        if (instance == null) { // 檢查實例是否已創建
            synchronized (Singleton.class) { // 確保線程安全
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

    public void showMessage() {
        System.out.println("Hello from Singleton!");
    }
}