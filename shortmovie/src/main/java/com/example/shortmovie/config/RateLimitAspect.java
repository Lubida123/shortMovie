package com.example.shortmovie.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.shortmovie.exception.RateLimitException;
import com.example.shortmovie.service.RateLimiterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流切面
 * 拦截带有@RateLimit注解的方法，执行限流检查
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    
    private final RateLimiterService rateLimiterService;
    
    @Around("@annotation(com.example.shortmovie.config.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);
        
        // 获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // 未认证用户，直接放行（由其他认证机制处理）
            return joinPoint.proceed();
        }
        
        String userId = authentication.getName();
        String rateLimitKey = rateLimit.keyPrefix() + ":" + userId;
        
        // 检查是否超过限流阈值
        boolean allowed = rateLimiterService.allowRequest(
            rateLimitKey, 
            rateLimit.maxRequests(), 
            rateLimit.windowSeconds()
        );
        
        if (!allowed) {
            long remaining = rateLimiterService.getRemainingRequests(
                rateLimitKey, 
                rateLimit.maxRequests(), 
                rateLimit.windowSeconds()
            );
            
            log.warn("Rate limit exceeded for user: {}, remaining: {}", userId, remaining);
            throw new RateLimitException(
                String.format("请求过于频繁，请稍后再试。每%d秒最多允许%d次请求", 
                    rateLimit.windowSeconds(), 
                    rateLimit.maxRequests())
            );
        }
        
        return joinPoint.proceed();
    }
}
