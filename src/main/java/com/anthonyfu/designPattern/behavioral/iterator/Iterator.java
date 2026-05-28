package com.anthonyfu.designPattern.behavioral.iterator;

//迭代器接口
interface Iterator<T> {
	boolean hasNext();
	T next();
}
