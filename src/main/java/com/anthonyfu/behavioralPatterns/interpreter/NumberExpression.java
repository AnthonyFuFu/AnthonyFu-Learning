package com.anthonyfu.behavioralPatterns.interpreter;

import java.util.Map;

//終端表達式 - 數字
class NumberExpression implements Expression {
	private int number;

	public NumberExpression(int number) {
		this.number = number;
	}

	@Override
	public int interpret(Map<String, Integer> context) {
		return number;
	}
}
