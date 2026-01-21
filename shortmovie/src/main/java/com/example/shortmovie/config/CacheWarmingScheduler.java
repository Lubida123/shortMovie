package com.example.shortmovie.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.shortmovie.service.CacheWarmingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存预热调度器
 * 在系统启动时和定期执行缓存预热任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmingScheduler implements ApplicationRunner {
    
    private final CacheWarmingService cacheWarmingService;
    
    /**
     * 系统启动时执行缓存预热
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("系统启动，开始执行推荐缓存预热...");
        try {
            // 预热前1000个活跃用户的推荐缓存（异步执行）
            cacheWarmingService.warmupRecommendationCache(1000);
            log.info("系统启动缓存预热任务已提交");
        } catch (Exception e) {
            log.error("系统启动缓存预热失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定期执行缓存预热（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledWarmup() {
        log.info("开始定期推荐缓存预热...");
        try {
            // 预热前500个活跃用户的推荐缓存（异步执行）
            cacheWarmingService.warmupRecommendationCache(500);
            log.info("定期缓存预热任务已提交");
        } catch (Exception e) {
            log.error("定期缓存预热失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定期清理过期缓存（每天凌晨3点执行）
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledCleanup() {
        log.info("开始清理过期推荐缓存...");
        try {
            int cleanedCount = cacheWarmingService.cleanExpiredCache();
            log.info("过期缓存清理完成，清理数量: {}", cleanedCount);
        } catch (Exception e) {
            log.error("清理过期缓存失败: {}", e.getMessage(), e);
        }
    }
}
