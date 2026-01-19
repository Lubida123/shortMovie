package com.example.shortmovie.service;

/**
 * 限流服务接口
 */
public interface RateLimiterService {
    
    /**
     * 检查是否允许请求
     * 使用滑动窗口算法实现限流
     * 
     * @param key 限流键（通常是用户ID）
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口大小（秒）
     * @return true表示允许请求，false表示超过限流阈值
     */
    boolean allowRequest(String key, int maxRequests, int windowSeconds);
    
    /**
     * 获取剩余请求次数
     * 
     * @param key 限流键
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口大小（秒）
     * @return 剩余请求次数
     */
    long getRemainingRequests(String key, int maxRequests, int windowSeconds);
}
