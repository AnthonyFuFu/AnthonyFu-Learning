package com.anthonyfu.designPattern.behavioral.memento;

import java.util.ArrayList;
import java.util.List;

//管理者 - 保存備忘錄
class CareTaker {
	private List<Memento> mementoList = new ArrayList<>();

	public void addMemento(Memento memento) {
		mementoList.add(memento);
	}

	public Memento getMemento(int index) {
		return mementoList.get(index);
	}
}