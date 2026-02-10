package com.AnthonyFuLearning.creationalPatterns.prototype;

import java.util.HashMap;
import java.util.Map;

//原型管理器 - 管理多個原型
class PrototypeManager {
	private static Map<String, Prototype> prototypes = new HashMap<>();

	public static void addPrototype(String key, Prototype prototype) {
		prototypes.put(key, prototype);
	}

	public static Prototype getPrototype(String key) {
		Prototype prototype = prototypes.get(key);
		if (prototype != null) {
			return prototype.clone();
		}
		return null;
	}
}
