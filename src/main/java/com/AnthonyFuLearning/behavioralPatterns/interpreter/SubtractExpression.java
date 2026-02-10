package com.AnthonyFuLearning.behavioralPatterns.interpreter;

import java.util.Map;

//非終端表達式 - 減法
class SubtractExpression implements Expression {
	private Expression leftExpression;
	private Expression rightExpression;

	public SubtractExpression(Expression left, Expression right) {
		this.leftExpression = left;
		this.rightExpression = right;
	}

	@Override
	public int interpret(Map<String, Integer> context) {
		return leftExpression.interpret(context) - rightExpression.interpret(context);
	}
}
