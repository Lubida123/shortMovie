package com.example.shortmovie.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 用于标记需要限流的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 时间窗口内最大请求数
     */
    int maxRequests() default 5;
    
    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 60;
    
    /**
     * 限流键前缀
     */
    String keyPrefix() default "comment";
}
