package com.AnthonyFuLearning.structuralPatterns.proxy;

public class ProxyPattern {
    public static void main(String[] args) {
    	// 代理模式：用於需要控制對資源的訪問的情況，如遠程代理（遠程訪問對象）、虛擬代理（延遲實例化）、保護代理（權限控制）等。
    	// 代理模式為其他對象提供一種代理，以控制對這個對象的訪問。
        // 使用代理加載和顯示圖像
        Image image = new ProxyImage("sample.jpg");
        
        // 首次調用會加載圖像
        System.out.println("First time display:");
        image.display();
        
        // 再次調用不會重新加載
        System.out.println("\nSecond time display:");
        image.display();
    }
}
