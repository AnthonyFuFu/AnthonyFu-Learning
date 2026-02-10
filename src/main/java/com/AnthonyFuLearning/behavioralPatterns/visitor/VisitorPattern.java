package com.AnthonyFuLearning.behavioralPatterns.visitor;

import java.util.ArrayList;
import java.util.List;

//測試訪問者模式
public class VisitorPattern {
	public static void main(String[] args) {
		// 創建元素
		List<ItemElement> items = new ArrayList<>();
		items.add(new Book("Design Patterns", 49.99));
		items.add(new Book("Clean Code", 39.99));
		items.add(new Fruit("Apple", 2.99, 2.5));
		items.add(new Fruit("Banana", 1.99, 3.0));

		// 創建訪問者並訪問元素
		Visitor visitor = new ShoppingCartVisitor();
//		double totalPrice = 0;

		for (ItemElement item : items) {
			item.accept(visitor);
		}
	}
}