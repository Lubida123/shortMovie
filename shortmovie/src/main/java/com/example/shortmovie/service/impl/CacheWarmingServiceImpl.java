package com.example.shortmovie.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.RecommendationResult;
import com.example.shortmovie.mapper.BehaviorRecordMapper;
import com.example.shortmovie.mapper.RecommendationResultMapper;
import com.example.shortmovie.service.CacheWarmingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存预热服务实现
 * 用于提升推荐系统性能，减少首次查询延迟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmingServiceImpl implements CacheWarmingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendationResultMapper recommendationResultMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    
    private static final String RECOMMEND_USER_PREFIX = "recommend:user:";
    private static final int CACHE_TTL_SECONDS = 3600; // 1小时
    
    @Override
    @Async("cacheWarmingExecutor")
    public void warmupRecommendationCache(int limit) {
        log.info("开始预热推荐缓存，限制用户数量: {}", limit);
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        
        try {
            // 1. 获取最近活跃的用户列表（根据最近7天的行为记录）
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.ge(BehaviorRecord::getCreateTime, sevenDaysAgo)
                    .select(BehaviorRecord::getUserId)
                    .groupBy(BehaviorRecord::getUserId)
                    .last("LIMIT " + limit);
            
            List<Long> activeUserIds = behaviorRecordMapper.selectList(queryWrapper)
                    .stream()
                    .map(BehaviorRecord::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            log.info("找到 {} 个活跃用户需要预热", activeUserIds.size());
            
            // 2. 为每个活跃用户预热推荐缓存
            for (Long userId : activeUserIds) {
                try {
                    if (warmupUserRecommendation(userId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("预热用户 {} 的推荐缓存失败: {}", userId, e.getMessage());
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("推荐缓存预热完成，成功: {}/{}, 耗时: {}ms", 
                    successCount, activeUserIds.size(), duration);
            
        } catch (Exception e) {
            log.error("预热推荐缓存失败: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean warmupUserRecommendation(Long userId) {
        try {
            String redisKey = RECOMMEND_USER_PREFIX + userId;
            
            // 检查缓存是否已存在且未过期
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                log.debug("用户 {} 的推荐缓存已存在，跳过预热", userId);
                return true;
            }
            
            // 从数据库查询离线推荐结果
            LambdaQueryWrapper<RecommendationResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RecommendationResult::getUserId, userId)
                    .eq(RecommendationResult::getType, "OFFLINE")
                    .orderByAsc(RecommendationResult::getRank)
                    .last("LIMIT 20");
            
            List<RecommendationResult> recommendations = recommendationResultMapper.selectList(queryWrapper);
            
            if (recommendations.isEmpty()) {
                log.debug("用户 {} 没有离线推荐结果，跳过预热", userId);
                return false;
            }
            
            // 提取视频ID列表并缓存到Redis
            List<Long> videoIds = recommendations.stream()
                    .map(RecommendationResult::getMovieId)
                    .collect(Collectors.toList());
            
            // 使用Redis List存储推荐结果
            redisTemplate.opsForList().rightPushAll(redisKey, videoIds.toArray());
            redisTemplate.expire(redisKey, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            
            log.debug("成功预热用户 {} 的推荐缓存，视频数量: {}", userId, videoIds.size());
            return true;
            
        } catch (Exception e) {
            log.error("预热用户 {} 的推荐缓存失败: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int cleanExpiredCache() {
        log.info("开始清理过期的推荐缓存");
        int cleanedCount = 0;
        
        try {
            // Redis的TTL机制会自动清理过期key，这里主要是记录日志
            // 如果需要手动清理，可以扫描所有推荐缓存key并检查TTL
            
            // 注意：SCAN命令在生产环境中要谨慎使用，可能影响性能
            // 这里仅作为示例，实际使用时建议通过Redis的过期策略自动清理
            
            log.info("推荐缓存清理完成，清理数量: {}", cleanedCount);
            
        } catch (Exception e) {
            log.error("清理过期缓存失败: {}", e.getMessage(), e);
        }
        
        return cleanedCount;
    }
}
