package com.anthonyfu.behavioralPatterns.iterator;

import java.util.ArrayList;
import java.util.List;

//具體容器實現 - 名字集合
class NameRepository implements Container<String> {
	private List<String> names = new ArrayList<>();

	public NameRepository() {
		names.add("Alice");
		names.add("Bob");
		names.add("Charlie");
		names.add("David");
	}

	// 添加自定義方法
	public void addName(String name) {
		names.add(name);
	}

	@Override
	public Iterator<String> getIterator() {
		return new NameIterator();
	}

	// 具體迭代器實現，作為內部類
	private class NameIterator implements Iterator<String> {
		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < names.size();
		}

		@Override
		public String next() {
			if (hasNext()) {
				return names.get(index++);
			}
			return null;
		}
	}
}
