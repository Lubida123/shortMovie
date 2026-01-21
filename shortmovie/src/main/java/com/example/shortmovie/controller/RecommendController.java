package com.example.shortmovie.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shortmovie.service.RecommendService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 推荐控制器
 */
@Slf4j
@Tag(name = "推荐接口")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {
    
    private final RecommendService recommendService;
    
    @Operation(summary = "获取实时推荐", description = "从Redis获取用户的实时推荐视频列表，需要用户登录")
    @GetMapping("/realtime")
    public R<List<VideoVO>> getRealtimeRecommend(Authentication authentication) {
        // 验证用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return R.error(401, "用户未登录");
        }
        
        try {
            // 从认证信息中获取用户ID
            Long userId = Long.parseLong(authentication.getName());
            
            // 获取实时推荐
            List<VideoVO> recommendations = recommendService.getRealtimeRecommendFromRedis(userId);
            
            // 如果Redis中没有推荐结果，降级返回热门视频
            if (recommendations.isEmpty()) {
                log.info("No realtime recommendations found for userId={}, fallback to hot videos", userId);
                recommendations = recommendService.getHotVideos(10);
            }
            
            return R.ok(recommendations);
            
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format in authentication: {}", authentication.getName());
            return R.error(400, "无效的用户ID");
        } catch (Exception e) {
            log.error("Failed to get realtime recommendations: {}", e.getMessage(), e);
            return R.error(500, "获取实时推荐失败");
        }
    }
    
    @Operation(summary = "获取离线推荐", description = "从MySQL获取用户的离线推荐视频列表（分页），需要用户登录")
    @GetMapping("/offline")
    public R<PageVO<VideoVO>> getOfflineRecommend(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            
            Authentication authentication
    ) {
        // 验证用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return R.error(401, "用户未登录");
        }
        
        try {
            // 从认证信息中获取用户ID
            Long userId = Long.parseLong(authentication.getName());
            
            // 获取离线推荐
            PageVO<VideoVO> recommendations = recommendService.getOfflineRecommendFromDB(userId, pageNum, pageSize);
            
            return R.ok(recommendations);
            
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format in authentication: {}", authentication.getName());
            return R.error(400, "无效的用户ID");
        } catch (Exception e) {
            log.error("Failed to get offline recommendations: {}", e.getMessage(), e);
            return R.error(500, "获取离线推荐失败");
        }
    }
    
    @Operation(summary = "获取混合推荐", description = "优先返回实时推荐，不足时补充离线推荐（分页），需要用户登录")
    @GetMapping("/hybrid")
    public R<PageVO<VideoVO>> getHybridRecommend(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            
            Authentication authentication
    ) {
        // 验证用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return R.error(401, "用户未登录");
        }
        
        try {
            // 从认证信息中获取用户ID
            Long userId = Long.parseLong(authentication.getName());
            
            // 获取混合推荐
            PageVO<VideoVO> recommendations = recommendService.getHybridRecommend(userId, pageNum, pageSize);
            
            return R.ok(recommendations);
            
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format in authentication: {}", authentication.getName());
            return R.error(400, "无效的用户ID");
        } catch (Exception e) {
            log.error("Failed to get hybrid recommendations: {}", e.getMessage(), e);
            return R.error(500, "获取混合推荐失败");
        }
    }
    
    @Operation(summary = "获取热门视频", description = "获取热门视频列表，不需要登录")
    @GetMapping("/hot")
    public R<List<VideoVO>> getHotVideos(
            @Parameter(description = "返回数量", example = "50")
            @RequestParam(defaultValue = "50") Integer limit
    ) {
        try {
            // 获取热门视频
            List<VideoVO> hotVideos = recommendService.getHotVideos(limit);
            
            return R.ok(hotVideos);
            
        } catch (Exception e) {
            log.error("Failed to get hot videos: {}", e.getMessage(), e);
            return R.error(500, "获取热门视频失败");
        }
    }
}
