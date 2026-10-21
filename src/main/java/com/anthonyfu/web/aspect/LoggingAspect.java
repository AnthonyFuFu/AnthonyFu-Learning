package com.anthonyfu.web.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // 定義切點
    @Pointcut("execution(* com.anthonyfu.web.service.*.*(..))")
    public void serviceLayer() {}
    
    // 前置通知
    @Before("serviceLayer()")
    public void beforeAdvice(JoinPoint joinPoint) {
//        log.info("Before method: " + joinPoint.getSignature().getName());
    }
    
    // 後置通知
    @After("serviceLayer()")
    public void afterAdvice(JoinPoint joinPoint) {
//        log.info("After method: " + joinPoint.getSignature().getName());
    }
    
    // 返回通知
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
//        log.info("AfterReturning Method " + joinPoint.getSignature().getName() + " returned with value: " + result);
    }
    
    // 異常通知
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "error")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable error) {
    	log.info("AfterThrowing Method " + joinPoint.getSignature().getName() + " throws exception: " + error);
    }
    
    // 環繞通知
    @Around("serviceLayer()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
//        long endTime = System.currentTimeMillis();
//        log.info("Around Method " + joinPoint.getSignature().getName() + " execution time: " + (endTime - startTime) + "ms");
        
        return result;
    }
}
