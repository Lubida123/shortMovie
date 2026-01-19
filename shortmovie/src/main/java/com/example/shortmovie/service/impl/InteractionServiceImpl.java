package com.example.shortmovie.service.impl;

import org.springframework.stereotype.Service;

import com.example.shortmovie.service.CollectService;
import com.example.shortmovie.service.InteractionService;
import com.example.shortmovie.service.LikeService;
import com.example.shortmovie.vo.VideoInteractionVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 交互状态服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final LikeService likeService;
    private final CollectService collectService;

    /**
     * 查询用户对视频的交互状态（点赞+收藏）
     * 优化：同时查询点赞和收藏状态，减少数据库访问次数
     *
     * @param userId  用户ID（可为null，表示未登录用户）
     * @param videoId 视频ID
     * @return 交互状态响应
     */
    @Override
    public VideoInteractionVO getInteractionStatus(Long userId, Long videoId) {
        VideoInteractionVO vo = new VideoInteractionVO();
        
        // 如果用户未登录，返回默认状态（未点赞、未收藏）
        if (userId == null) {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
            return vo;
        }
        
        // 同时查询点赞和收藏状态
        // 这里通过调用已有的服务方法来实现
        // 每个方法内部只执行一次数据库查询
        Boolean isLiked = likeService.isLiked(userId, videoId);
        Boolean isCollected = collectService.isCollected(userId, videoId);
        
        vo.setIsLiked(isLiked);
        vo.setIsCollected(isCollected);
        
        return vo;
    }
}
