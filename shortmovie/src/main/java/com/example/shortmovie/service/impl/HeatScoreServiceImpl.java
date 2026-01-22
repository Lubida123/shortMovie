package com.example.shortmovie.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.shortmovie.config.RecommendationProperties;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.HeatScoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 热度分数计算服务实现
 * 
 * 热度计算公式：
 * heatScore = (playCount * playWeight + likeCount * likeWeight 
 *            + commentCount * commentWeight + collectCount * collectWeight) 
 *            * timeDecayFactor
 * 
 * 时间衰减因子：
 * - 7天内：1.0（无衰减）
 * - 7-30天：0.8
 * - 30-90天：0.5
 * - 90天以上：0.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeatScoreServiceImpl implements HeatScoreService {
    
    private final VideoMapper videoMapper;
    private final RecommendationProperties recommendationProperties;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    
    // Redis Key前缀
    private static final String HOT_VIDEOS_CACHE_PREFIX = "hot_videos:";
    
    // 时间衰减阈值（天）
    private static final int DECAY_THRESHOLD_1 = 7;
    private static final int DECAY_THRESHOLD_2 = 30;
    private static final int DECAY_THRESHOLD_3 = 90;
    
    // 时间衰减因子
    private static final double DECAY_FACTOR_NEW = 1.0;      // 7天内
    private static final double DECAY_FACTOR_RECENT = 0.8;   // 7-30天
    private static final double DECAY_FACTOR_MEDIUM = 0.5;   // 30-90天
    private static final double DECAY_FACTOR_OLD = 0.3;      // 90天以上
    
    @Override
    public Double calculateVideoHeatScore(Long videoId) {
        try {
            // 查询视频信息
            Video video = videoMapper.selectById(videoId);
            if (video == null) {
                log.warn("Video not found for heatScore calculation: videoId={}", videoId);
                return 0.0;
            }
            
            return calculateHeatScore(video);
            
        } catch (Exception e) {
            log.error("Failed to calculate heat score for videoId={}: {}", videoId, e.getMessage(), e);
            return 0.0;
        }
    }
    
    @Override
    @Transactional
    public void updateVideoHeatScore(Long videoId) {
        try {
            // 计算热度分数
            Double heatScore = calculateVideoHeatScore(videoId);
            
            // 更新到数据库
            LambdaUpdateWrapper<Video> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Video::getId, videoId)
                    .set(Video::getHeatScore, BigDecimal.valueOf(heatScore).setScale(2, RoundingMode.HALF_UP));
            
            int updated = videoMapper.update(null, updateWrapper);
            
            if (updated > 0) {
                log.debug("Updated heat score for videoId={}: {}", videoId, heatScore);
            }
            
        } catch (Exception e) {
            log.error("Failed to update heat score for videoId={}: {}", videoId, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public Integer updateAllVideoHeatScores() {
        try {
            log.info("开始批量更新视频热度分数...");
            
            // 查询所有已审核的视频
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Video::getAuditStatus, 1)
                    .eq(Video::getIsDeleted, 0);
            
            List<Video> videos = videoMapper.selectList(queryWrapper);
            
            if (videos == null || videos.isEmpty()) {
                log.warn("No videos found for heat score update");
                return 0;
            }
            
            int updatedCount = 0;
            
            // 批量更新热度分数
            for (Video video : videos) {
                try {
                    Double heatScore = calculateHeatScore(video);
                    
                    LambdaUpdateWrapper<Video> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Video::getId, video.getId())
                            .set(Video::getHeatScore, BigDecimal.valueOf(heatScore).setScale(2, RoundingMode.HALF_UP));
                    
                    videoMapper.update(null, updateWrapper);
                    updatedCount++;
                    
                } catch (Exception e) {
                    log.error("Failed to update heat score for videoId={}: {}", video.getId(), e.getMessage());
                }
            }
            
            // 清除热门视频缓存
            clearHotVideosCache();
            
            log.info("✅ 批量更新视频热度分数完成，共更新 {} 个视频", updatedCount);
            return updatedCount;
            
        } catch (Exception e) {
            log.error("Failed to update all video heat scores: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 清除热门视频缓存
     */
    private void clearHotVideosCache() {
        try {
            // 删除所有热门视频缓存（支持通配符）
            java.util.Set<String> keys = redisTemplate.keys(HOT_VIDEOS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} hot videos cache entries", keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to clear hot videos cache: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Integer updateHotVideoFlags(Integer topN) {
        try {
            log.info("开始更新热门视频标记，TOP {}", topN);
            
            // 1. 先将所有视频的isHot设置为0
            LambdaUpdateWrapper<Video> resetWrapper = new LambdaUpdateWrapper<>();
            resetWrapper.eq(Video::getAuditStatus, 1)
                    .set(Video::getIsHot, 0);
            videoMapper.update(null, resetWrapper);
            
            // 2. 查询热度分数最高的TOP N视频
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Video::getAuditStatus, 1)
                    .eq(Video::getIsDeleted, 0)
                    .orderByDesc(Video::getHeatScore)
                    .last("LIMIT " + topN);
            
            List<Video> topVideos = videoMapper.selectList(queryWrapper);
            
            if (topVideos == null || topVideos.isEmpty()) {
                log.warn("No videos found for hot flag update");
                return 0;
            }
            
            // 3. 将TOP N视频标记为热门
            List<Long> topVideoIds = topVideos.stream()
                    .map(Video::getId)
                    .toList();
            
            LambdaUpdateWrapper<Video> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(Video::getId, topVideoIds)
                    .set(Video::getIsHot, 1);
            
            int updated = videoMapper.update(null, updateWrapper);
            
            log.info("✅ 热门视频标记更新完成，共标记 {} 个视频", updated);
            return updated;
            
        } catch (Exception e) {
            log.error("Failed to update hot video flags: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 计算视频热度分数（核心算法）
     */
    private Double calculateHeatScore(Video video) {
        // 1. 获取权重配置
        Double playWeight = getWeight("play");
        Double likeWeight = getWeight("like");
        Double commentWeight = getWeight("comment");
        Double collectWeight = getWeight("collect");
        
        // 2. 计算基础热度分数
        double baseScore = (video.getPlayCount() != null ? video.getPlayCount() : 0L) * playWeight
                + (video.getLikeCount() != null ? video.getLikeCount() : 0L) * likeWeight
                + (video.getCommentCount() != null ? video.getCommentCount() : 0L) * commentWeight
                + (video.getCollectCount() != null ? video.getCollectCount() : 0L) * collectWeight;
        
        // 3. 计算时间衰减因子
        double timeDecayFactor = calculateTimeDecayFactor(video.getCreateTime());
        
        // 4. 最终热度分数 = 基础分数 * 时间衰减因子
        double finalScore = baseScore * timeDecayFactor;
        
        log.debug("Video {} heat score: base={}, decay={}, final={}", 
                video.getId(), baseScore, timeDecayFactor, finalScore);
        
        return finalScore;
    }
    
    /**
     * 计算时间衰减因子
     * 新视频权重更高，随时间逐渐降低
     */
    private double calculateTimeDecayFactor(LocalDateTime createTime) {
        if (createTime == null) {
            return DECAY_FACTOR_OLD;
        }
        
        // 计算视频发布距今的天数
        long daysSinceCreation = ChronoUnit.DAYS.between(createTime, LocalDateTime.now());
        
        if (daysSinceCreation <= DECAY_THRESHOLD_1) {
            return DECAY_FACTOR_NEW;      // 7天内：无衰减
        } else if (daysSinceCreation <= DECAY_THRESHOLD_2) {
            return DECAY_FACTOR_RECENT;   // 7-30天：轻微衰减
        } else if (daysSinceCreation <= DECAY_THRESHOLD_3) {
            return DECAY_FACTOR_MEDIUM;   // 30-90天：中度衰减
        } else {
            return DECAY_FACTOR_OLD;      // 90天以上：重度衰减
        }
    }
    
    /**
     * 获取权重配置，如果未配置则使用默认值
     */
    private Double getWeight(String key) {
        if (recommendationProperties.getHeatScoreWeights() != null 
                && recommendationProperties.getHeatScoreWeights().containsKey(key)) {
            return recommendationProperties.getHeatScoreWeights().get(key);
        }
        
        // 默认权重
        return switch (key) {
            case "play" -> 1.0;
            case "like" -> 3.0;
            case "comment" -> 5.0;
            case "collect" -> 8.0;
            default -> 1.0;
        };
    }
}
