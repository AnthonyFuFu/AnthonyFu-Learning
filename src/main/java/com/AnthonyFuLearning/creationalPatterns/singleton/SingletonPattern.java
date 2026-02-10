package com.AnthonyFuLearning.creationalPatterns.singleton;

public class SingletonPattern {
    public static void main(String[] args) {
    	// 單例模式確保一個類只有一個實例，並提供全局訪問點。
        Singleton singleton = Singleton.getInstance();
        singleton.showMessage(); // 輸出: Hello from Singleton!
    }
}
