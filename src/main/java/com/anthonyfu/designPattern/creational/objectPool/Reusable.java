package com.anthonyfu.designPattern.creational.objectPool;

//可重用對象接口
interface Reusable {
	void reset(); // 重置對象狀態以供重用
	void operation(); // 對象的實際操作
}