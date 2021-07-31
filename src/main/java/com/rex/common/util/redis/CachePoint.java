package com.rex.common.util.redis;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CachePoint {

    // 切入点（设置缓存）
    @Pointcut("@annotation(com.rex.common.util.redis.Cache)")
    public void cache() {
        System.out.println("进入@Cache");
    }

    // 前置通知
    @Before(value = "cache()")
    public void doBefore(JoinPoint joinPoint) {
        System.out.println("触发到 @Before");
    }

    // 后置通知
    @AfterReturning(value = "cache() && @annotation(ca)", returning = "value")
    public void doAfterReturning(JoinPoint joinPoint, Object value, Cache ca) {
        // 获取自定义注解的值
        System.out.println("key值：" + ca.key());
        System.out.println("val值：" + ca.val());
        // 获得执行方法的类名
        String targetName = joinPoint.getTarget().getClass().getName();
        // 获得执行方法的方法名
        String methodName = joinPoint.getSignature().getName();
        System.out.println("触发 @AfterReturning");
        Object[] args = joinPoint.getArgs();
        for (Object o : args) {
            System.out.println(o);
        }
        System.out.println(value.toString());
        String key = ca.key() + args[0];
        RedisUtil.setCache(key, JSON.toJSONString(value));
    }


    // 切入点（去除缓存）
    @Pointcut("@annotation(com.rex.common.util.redis.CacheDel)")
    public void CacheDel() {
        System.out.println("进入@CacheDel");
    }

    // 后置通知
    @AfterReturning(value = "CacheDel() && @annotation(del)")
    public void doAfterReturning(JoinPoint joinPoint, CacheDel del) {
        Object[] args = joinPoint.getArgs();
        String key = del.key() + args[0];
        RedisUtil.delCache(key);
    }
}