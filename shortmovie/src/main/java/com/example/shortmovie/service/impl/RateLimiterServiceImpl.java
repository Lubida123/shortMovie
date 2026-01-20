package com.example.shortmovie.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.shortmovie.service.RateLimiterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 限流服务实现
 * 使用Redis的Sorted Set实现滑动窗口限流算法
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    @Override
    public boolean allowRequest(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - windowSeconds * 1000L;
        
        try {
            // 1. 删除窗口之外的旧记录
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            
            // 2. 统计当前窗口内的请求数
            Long count = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
            
            if (count != null && count >= maxRequests) {
                log.warn("Rate limit exceeded for key: {}, count: {}, max: {}", key, count, maxRequests);
                return false;
            }
            
            // 3. 添加当前请求到窗口
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
            
            // 4. 设置过期时间（窗口大小的2倍，确保数据清理）
            redisTemplate.expire(redisKey, windowSeconds * 2L, TimeUnit.SECONDS);
            
            return true;
        } catch (Exception e) {
            log.error("Rate limiter error for key: {}", key, e);
            // 如果Redis出错，默认允许请求（降级策略）
            return true;
        }
    }
    
    @Override
    public long getRemainingRequests(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - windowSeconds * 1000L;
        
        try {
            // 删除窗口之外的旧记录
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            
            // 统计当前窗口内的请求数
            Long count = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
            
            if (count == null) {
                return maxRequests;
            }
            
            return Math.max(0, maxRequests - count);
        } catch (Exception e) {
            log.error("Error getting remaining requests for key: {}", key, e);
            return maxRequests;
        }
    }
}
