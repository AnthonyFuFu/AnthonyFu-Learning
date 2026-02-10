package com.AnthonyFuLearning.behavioralPatterns.visitor;

//具體元素 - 書籍
class Book implements ItemElement {
	private String name;
	private double price;

	public Book(String name, double price) {
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}