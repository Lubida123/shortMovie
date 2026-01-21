package com.example.shortmovie.service;

import java.util.List;

import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

/**
 * 推荐服务接口
 */
public interface RecommendService {
    
    /**
     * 从Redis获取实时推荐
     * 
     * @param userId 用户ID
     * @return 推荐视频列表
     */
    List<VideoVO> getRealtimeRecommendFromRedis(Long userId);
    
    /**
     * 从MySQL获取离线推荐
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 推荐视频分页结果
     */
    PageVO<VideoVO> getOfflineRecommendFromDB(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取混合推荐（优先实时，不足时补充离线）
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 推荐视频分页结果
     */
    PageVO<VideoVO> getHybridRecommend(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取热门视频（冷启动降级）
     * 
     * @param limit 返回数量限制
     * @return 热门视频列表
     */
    List<VideoVO> getHotVideos(Integer limit);
    
    /**
     * 检查用户是否为新用户（无历史行为记录）
     * 
     * @param userId 用户ID
     * @return true-新用户，false-老用户
     */
    boolean isNewUser(Long userId);
    
    /**
     * 基于用户历史行为的分类和标签，推荐相似视频
     * 
     * @param userId 用户ID
     * @param limit 返回数量限制
     * @return 推荐视频列表
     */
    List<VideoVO> getCategoryBasedRecommendations(Long userId, Integer limit);
}
