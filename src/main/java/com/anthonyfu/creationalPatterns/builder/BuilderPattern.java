package com.anthonyfu.creationalPatterns.builder;

public class BuilderPattern {
    public static void main(String[] args) {
    	// 建造者模式用於分步創建複雜對象。
        ComputerBuilder builder = new GamingComputerBuilder();
        ComputerDirector director = new ComputerDirector(builder);
        Computer gamingPC = director.construct();
        System.out.println(gamingPC); // 輸出: Computer [CPU=High-end CPU, GPU=High-end GPU, RAM=32GB, Storage=1024GB]
    }
}
