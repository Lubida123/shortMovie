package com.example.shortmovie.service;

/**
 * 热度分数计算服务接口
 */
public interface HeatScoreService {
    
    /**
     * 计算单个视频的热度分数
     * @param videoId 视频ID
     * @return 热度分数
     */
    Double calculateVideoHeatScore(Long videoId);
    
    /**
     * 更新单个视频的热度分数
     * @param videoId 视频ID
     */
    void updateVideoHeatScore(Long videoId);
    
    /**
     * 批量更新所有视频的热度分数
     * @return 更新的视频数量
     */
    Integer updateAllVideoHeatScores();
    
    /**
     * 更新热门视频标记（基于热度分数排名）
     * @param topN 标记为热门的视频数量
     * @return 标记的视频数量
     */
    Integer updateHotVideoFlags(Integer topN);
}
