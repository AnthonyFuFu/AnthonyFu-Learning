package com.AnthonyFuLearning.structuralPatterns.bridge;

public class BridgePattern {
    public static void main(String[] args) {
    	// 橋接模式：用於將抽象與實現分離，使它們可以獨立變化，適用於多維度變化的系統。
    	// 橋接模式用於將抽象部分與實現部分分離，使它們可以獨立變化。
        // 使用紅色圈繪製器創建圓形
        Shape redCircle = new Circle(100, 100, 10, new RedCircleDrawer());
        // 使用綠色圈繪製器創建圓形
        Shape greenCircle = new Circle(200, 200, 20, new GreenCircleDrawer());
        
        redCircle.draw();
        greenCircle.draw();
    }
}