package com.anthonyfu.behavioralPatterns.iterator;

public class IteratorPattern {
    public static void main(String[] args) {
        NameRepository namesRepository = new NameRepository();
        
        // 添加更多名字
        namesRepository.addName("Eve");
        namesRepository.addName("Frank");
        
        // 獲取迭代器
        Iterator<String> iterator = namesRepository.getIterator();
        
        // 使用迭代器遍歷元素
        System.out.println("Names:");
        while (iterator.hasNext()) {
            String name = iterator.next();
            System.out.println(name);
        }
    }
}
