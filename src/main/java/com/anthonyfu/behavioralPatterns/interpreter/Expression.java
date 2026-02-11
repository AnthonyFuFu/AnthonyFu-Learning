package com.anthonyfu.behavioralPatterns.interpreter;

import java.util.Map;

//表達式接口
interface Expression {
	int interpret(Map<String, Integer> context);
}