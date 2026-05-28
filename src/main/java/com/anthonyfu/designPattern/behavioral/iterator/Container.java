package com.anthonyfu.designPattern.behavioral.iterator;

//容器接口
interface Container<T> {
	Iterator<T> getIterator();
}
