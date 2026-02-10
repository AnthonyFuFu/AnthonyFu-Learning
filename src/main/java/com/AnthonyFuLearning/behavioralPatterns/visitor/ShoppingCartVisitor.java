package com.AnthonyFuLearning.behavioralPatterns.visitor;

//具體訪問者 - 計算總價
class ShoppingCartVisitor implements Visitor {
	@Override
	public void visit(Book book) {
		double cost = book.getPrice();
		System.out.println("Book: " + book.getName() + " - Price: $" + cost);
	}

	@Override
	public void visit(Fruit fruit) {
		double cost = fruit.getPricePerKg() * fruit.getWeight();
		System.out.println("Fruit: " + fruit.getName() + " - Price: $" + cost);
	}
}
