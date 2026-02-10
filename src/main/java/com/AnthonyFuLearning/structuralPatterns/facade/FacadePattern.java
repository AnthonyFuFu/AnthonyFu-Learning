package com.AnthonyFuLearning.structuralPatterns.facade;

public class FacadePattern {
    public static void main(String[] args) {
    	// 外觀模式：用於簡化複雜子系統的使用，提供一個統一的接口。
    	// 外觀模式提供了一個統一的接口，用來訪問子系統中的一組接口，簡化了客戶端與複雜系統的交互。
        ComputerFacade computer = new ComputerFacade();
        computer.start();
    }
}
