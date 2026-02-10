package com.AnthonyFuLearning.structuralPatterns.composite;

public class CompositePattern {
    public static void main(String[] args) {
    	// 組合模式：用於處理樹形結構，如文件系統、組織結構等。
    	// 組合模式將對象組合成樹形結構，來表示"部分-整體"的層次結構，使客戶能以一致的方式處理單個對象和組合對象。
        // 創建組合結構
        Component root = new Composite("Root");
        Component branch1 = new Composite("Branch 1");
        Component branch2 = new Composite("Branch 2");
        Component leaf1 = new Leaf("Leaf 1");
        Component leaf2 = new Leaf("Leaf 2");
        Component leaf3 = new Leaf("Leaf 3");
        
        // 組裝樹結構
        root.add(branch1);
        root.add(branch2);
        branch1.add(leaf1);
        branch1.add(leaf2);
        branch2.add(leaf3);
        
        // 對整個結構執行操作
        root.operation();
    }
}
