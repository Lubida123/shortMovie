package com.example.shortmovie.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.shortmovie.service.HeatScoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 热度分数定时更新调度器
 * 
 * 定时任务：
 * 1. 每小时更新一次所有视频的热度分数
 * 2. 每天凌晨2点更新热门视频标记
 * 3. 应用启动时执行一次初始化更新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeatScoreScheduler {
    
    private final HeatScoreService heatScoreService;
    
    // 热门视频数量（TOP N）
    private static final int HOT_VIDEO_TOP_N = 50;
    
    /**
     * 应用启动时执行初始化更新
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("🚀 应用启动完成，开始初始化热度分数...");
        try {
            // 延迟10秒执行，避免启动时资源竞争
            Thread.sleep(10000);
            
            // 更新所有视频热度分数
            Integer updatedCount = heatScoreService.updateAllVideoHeatScores();
            log.info("✅ 初始化热度分数完成，更新了 {} 个视频", updatedCount);
            
            // 更新热门视频标记
            Integer hotCount = heatScoreService.updateHotVideoFlags(HOT_VIDEO_TOP_N);
            log.info("✅ 初始化热门视频标记完成，标记了 {} 个视频", hotCount);
            
        } catch (Exception e) {
            log.error("❌ 初始化热度分数失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定时更新所有视频的热度分数
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateHeatScores() {
        log.info("⏰ 开始定时更新视频热度分数...");
        try {
            Integer updatedCount = heatScoreService.updateAllVideoHeatScores();
            log.info("✅ 定时更新热度分数完成，更新了 {} 个视频", updatedCount);
        } catch (Exception e) {
            log.error("❌ 定时更新热度分数失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定时更新热门视频标记
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void updateHotVideoFlags() {
        log.info("⏰ 开始定时更新热门视频标记...");
        try {
            Integer hotCount = heatScoreService.updateHotVideoFlags(HOT_VIDEO_TOP_N);
            log.info("✅ 定时更新热门视频标记完成，标记了 {} 个视频", hotCount);
        } catch (Exception e) {
            log.error("❌ 定时更新热门视频标记失败: {}", e.getMessage(), e);
        }
    }
}
