package com.AnthonyFuLearning.behavioralPatterns.iterator;

//迭代器接口
interface Iterator<T> {
	boolean hasNext();
	T next();
}
