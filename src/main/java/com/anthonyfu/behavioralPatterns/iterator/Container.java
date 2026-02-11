package com.anthonyfu.behavioralPatterns.iterator;

//容器接口
interface Container<T> {
	Iterator<T> getIterator();
}
