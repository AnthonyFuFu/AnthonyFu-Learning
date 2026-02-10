package com.AnthonyFuLearning.structuralPatterns.flyweight;

public class FlyweightPattern {
    private static final String[] colors = { "Red", "Green", "Blue", "Yellow", "Black" };
    
    public static void main(String[] args) {
    	// 享元模式 / 輕量模式：用於大量相似對象的高效共享，如字體渲染、遊戲中的粒子效果等。
    	// 享元模式 / 輕量模式用於減少創建對象的數量，以減少內存佔用和提高性能。
        // 使用工廠獲取共享對象
        for (int i = 0; i < 20; ++i) {
            Circle circle = (Circle)ShapeFactory.getCircle(getRandomColor());
            circle.setX(getRandomX());
            circle.setY(getRandomY());
            circle.setRadius(100);
            circle.draw();
        }
    }
    
    private static String getRandomColor() {
        return colors[(int)(Math.random() * colors.length)];
    }
    
    private static int getRandomX() {
        return (int)(Math.random() * 100);
    }
    
    private static int getRandomY() {
        return (int)(Math.random() * 100);
    }
}
