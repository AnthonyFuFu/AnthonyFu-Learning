package com.anthonyfu.behavioralPatterns.chainOfResponsibility;

//測試責任鏈模式
public class ChainOfResponsibilityPattern {
    public static void main(String[] args) {
        // 創建處理者
        Handler level1 = new Level1Handler();
        Handler level2 = new Level2Handler();
        Handler level3 = new Level3Handler();

        // 設置責任鏈
        level1.setSuccessor(level2);
        level2.setSuccessor(level3);

        // 處理請求
        level1.handleRequest(5);   // Level 1 處理
        level1.handleRequest(15);  // Level 2 處理
        level1.handleRequest(25);  // Level 3 處理
        level1.handleRequest(35);  // 超出處理範圍
    }
}
