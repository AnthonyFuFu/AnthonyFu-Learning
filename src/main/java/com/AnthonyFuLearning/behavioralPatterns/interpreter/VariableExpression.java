package com.AnthonyFuLearning.behavioralPatterns.interpreter;

import java.util.Map;

//終端表達式 - 變量
class VariableExpression implements Expression {
	private String name;

	public VariableExpression(String name) {
		this.name = name;
	}

	@Override
	public int interpret(Map<String, Integer> context) {
		if (context.containsKey(name)) {
			return context.get(name);
		}
		return 0;
	}
}
