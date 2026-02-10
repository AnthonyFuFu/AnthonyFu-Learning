package com.AnthonyFuLearning.behavioralPatterns.visitor;

//可被訪問的元素接口
interface ItemElement {
	void accept(Visitor visitor);
}
