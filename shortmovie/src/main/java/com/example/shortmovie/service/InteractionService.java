package com.example.shortmovie.service;

import com.example.shortmovie.vo.VideoInteractionVO;

/**
 * 交互状态服务接口
 */
public interface InteractionService {

    /**
     * 查询用户对视频的交互状态（点赞+收藏）
     *
     * @param userId  用户ID（可为null，表示未登录用户）
     * @param videoId 视频ID
     * @return 交互状态响应
     */
    VideoInteractionVO getInteractionStatus(Long userId, Long videoId);
}
