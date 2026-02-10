package com.AnthonyFuLearning.creationalPatterns.abstractFactory;

public class AbstractFactoryPattern {
    public static void main(String[] args) {
    	// 抽象工廠模式用於生成一系列相關的對象，而不指定具體類型。
        // 選擇 Windows 工廠
        GUIFactory factory = new WindowsFactory();
        Button button = factory.createButton();
        Checkbox checkbox = factory.createCheckbox();
        button.click(); // 輸出: Click Windows Button
        checkbox.check(); // 輸出: Check Windows Checkbox

        // 替換為 Mac 工廠
        factory = new MacFactory();
        button = factory.createButton();
        checkbox = factory.createCheckbox();
        button.click(); // 輸出: Click Mac Button
        checkbox.check(); // 輸出: Check Mac Checkbox
    }
}