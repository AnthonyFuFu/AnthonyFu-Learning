package com.anthonyfu.behavioralPatterns.visitor;

//訪問者接口
interface Visitor {
	void visit(Book book);
	void visit(Fruit fruit);
}