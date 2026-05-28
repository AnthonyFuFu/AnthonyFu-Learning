package com.anthonyfu.designPattern.behavioral.visitor;

//可被訪問的元素接口
interface ItemElement {
	void accept(Visitor visitor);
}
