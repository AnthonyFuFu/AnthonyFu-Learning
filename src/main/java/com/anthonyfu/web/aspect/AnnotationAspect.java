package com.anthonyfu.web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AnnotationAspect {
	@Around("@annotation(com.anthonyfu.web.aspect.LogExecutionTime)")
	public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();

		Object result = joinPoint.proceed();

		long endTime = System.currentTimeMillis();
		System.out.println(joinPoint.getSignature().getName() + " executed in " + (endTime - startTime) + "ms");

		return result;
	}
}
