package com.anthonyfu.designPattern.behavioral.interpreter;

import java.util.HashMap;
import java.util.Map;

public class InterpreterPattern {
    public static void main(String[] args) {
        // 創建表達式: a + b - c
        Expression expression = new SubtractExpression(
            new AddExpression(
                new VariableExpression("a"),
                new VariableExpression("b")
            ),
            new VariableExpression("c")
        );
        
        // 創建上下文
        Map<String, Integer> context = new HashMap<>();
        context.put("a", 10);
        context.put("b", 5);
        context.put("c", 3);
        
        // 解釋表達式
        int result = expression.interpret(context);
        System.out.println("a + b - c = " + result);  // 應輸出 12
        
        // 更改變量值
        context.put("a", 20);
        result = expression.interpret(context);
        System.out.println("a + b - c = " + result);  // 應輸出 22
    }
}
