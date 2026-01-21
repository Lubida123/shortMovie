package com.example.shortmovie.service;

/**
 * 缓存预热服务接口
 * 用于在系统启动或定时任务中预加载推荐结果到Redis
 */
public interface CacheWarmingService {
    
    /**
     * 预热所有活跃用户的推荐结果缓存
     * @param limit 预热的用户数量限制
     */
    void warmupRecommendationCache(int limit);
    
    /**
     * 预热指定用户的推荐结果缓存
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean warmupUserRecommendation(Long userId);
    
    /**
     * 清理过期的推荐缓存
     * @return 清理的缓存数量
     */
    int cleanExpiredCache();
}
