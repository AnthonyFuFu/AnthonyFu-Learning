package com.anthonyfu.behavioralPatterns.interpreter;

import java.util.Map;

//非終端表達式 - 加法
class AddExpression implements Expression {
	private Expression leftExpression;
	private Expression rightExpression;

	public AddExpression(Expression left, Expression right) {
		this.leftExpression = left;
		this.rightExpression = right;
	}

	@Override
	public int interpret(Map<String, Integer> context) {
		return leftExpression.interpret(context) + rightExpression.interpret(context);
	}
}
