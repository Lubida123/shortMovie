package com.example.shortmovie.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.UserLike;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.exception.ResourceNotFoundException;
import com.example.shortmovie.mapper.UserLikeMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.FileStorageService;
import com.example.shortmovie.service.KafkaMessageProducer;
import com.example.shortmovie.service.LikeService;
import com.example.shortmovie.vo.LikeStatusVO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 点赞服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final UserLikeMapper userLikeMapper;
    private final VideoMapper videoMapper;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.example.shortmovie.mapper.BehaviorRecordMapper behaviorRecordMapper;

    @Autowired(required = false)
    private FileStorageService fileStorageService;
    
    // Redis缓存Key前缀
    private static final String VIDEO_DETAIL_CACHE_PREFIX = "video:detail:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeStatusVO toggleLike(Long userId, Long videoId) {
        // 验证视频是否存在
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }

        // 查询当前点赞状态
        UserLike existingLike = userLikeMapper.selectByUserIdAndVideoId(userId, videoId);

        boolean isLiked;
        if (existingLike == null) {
            // 创建点赞记录
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setVideoId(videoId);
            userLike.setCreateTime(LocalDateTime.now());
            userLike.setIsDeleted(0);
            
            try {
                userLikeMapper.insert(userLike);
                // 使用原子操作增加视频点赞计数
                videoMapper.incrementLikeCount(videoId);
                isLiked = true;
                log.info("用户 {} 点赞视频 {}", userId, videoId);
            } catch (DuplicateKeyException e) {
                // 并发冲突：另一个线程已经创建了点赞记录
                // 这种情况下，当前事务会回滚，我们需要重新执行toggle操作
                log.warn("并发点赞冲突，用户可能在多个请求中同时点赞: userId={}, videoId={}", userId, videoId);
                // 重新查询当前状态，如果已存在则执行取消点赞
                existingLike = userLikeMapper.selectByUserIdAndVideoId(userId, videoId);
                if (existingLike != null) {
                    // 已经点赞，执行取消点赞
                    userLikeMapper.deleteById(existingLike.getId());
                    videoMapper.decrementLikeCount(videoId);
                    isLiked = false;
                    log.info("并发冲突处理：用户 {} 取消点赞视频 {}", userId, videoId);
                } else {
                    // 理论上不应该到这里，说明记录在查询后又被删除了
                    // 这种极端情况下，我们认为操作失败，抛出异常让用户重试
                    throw new RuntimeException("并发冲突：点赞状态不一致，请重试");
                }
            }
        } else {
            // 删除点赞记录（逻辑删除）
            userLikeMapper.deleteById(existingLike.getId());

            // 使用原子操作减少视频点赞计数
            videoMapper.decrementLikeCount(videoId);

            isLiked = false;
            log.info("用户 {} 取消点赞视频 {}", userId, videoId);
        }

        // 重新查询视频获取最新的点赞计数
        video = videoMapper.selectById(videoId);

        // 创建行为记录并发送到Kafka
        BehaviorRecord behaviorRecord = new BehaviorRecord();
        behaviorRecord.setUserId(userId);
        behaviorRecord.setVideoId(videoId);
        behaviorRecord.setBehaviorType("LIKE");
        behaviorRecord.setCreateTime(LocalDateTime.now());

        try {
            // 1. 先保存到MySQL（持久化）
            behaviorRecordMapper.insert(behaviorRecord);
            log.debug("Like behavior saved to MySQL: userId={}, videoId={}", userId, videoId);
            
            // 2. 再发送到Kafka
            kafkaMessageProducer.sendBehaviorObject(behaviorRecord);
            log.debug("点赞行为记录已发送到Kafka: userId={}, videoId={}", userId, videoId);
        } catch (Exception e) {
            log.warn("发送点赞行为记录到Kafka失败: userId={}, videoId={}, error={}", userId, videoId, e.getMessage());
            // Kafka失败不影响用户操作
        }
        
        // 删除视频详情缓存（点赞数已更新）
        invalidateVideoDetailCache(videoId);
        
        // 异步更新热度分数
        updateHeatScoreAsync(videoId);

        // 构建响应
        LikeStatusVO response = new LikeStatusVO();
        response.setIsLiked(isLiked);
        response.setLikeCount(video.getLikeCount());

        return response;
    }
    
    /**
     * 删除视频详情缓存
     */
    private void invalidateVideoDetailCache(Long videoId) {
        try {
            String cacheKey = VIDEO_DETAIL_CACHE_PREFIX + videoId;
            redisTemplate.delete(cacheKey);
            log.debug("Video detail cache invalidated after like action: videoId={}", videoId);
        } catch (Exception e) {
            log.warn("Failed to invalidate video detail cache: videoId={}, error={}", videoId, e.getMessage());
        }
    }
    
    /**
     * 异步更新视频热度分数
     */
    private void updateHeatScoreAsync(Long videoId) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                com.example.shortmovie.service.HeatScoreService heatScoreService = 
                    org.springframework.context.ApplicationContextProvider.getApplicationContext()
                        .getBean(com.example.shortmovie.service.HeatScoreService.class);
                
                heatScoreService.updateVideoHeatScore(videoId);
                log.debug("Heat score updated asynchronously after like action for videoId={}", videoId);
            } catch (Exception e) {
                log.warn("Failed to update heat score asynchronously for videoId={}: {}", videoId, e.getMessage());
            }
        });
    }

    @Override
    public PageVO<VideoVO> getUserLikeList(Long userId, Integer pageNum, Integer pageSize) {
        // 验证分页参数
        validatePaginationParams(pageNum, pageSize);
        
        // 创建分页对象
        Page<Long> page = new Page<>(pageNum, pageSize);

        // 查询用户点赞的视频ID列表
        Page<Long> videoIdPage = (Page<Long>) userLikeMapper.selectVideoIdsByUserId(page, userId);

        // 根据视频ID列表查询视频详情
        List<VideoVO> videoVOList = videoIdPage.getRecords().stream()
                .map(videoId -> {
                    Video video = videoMapper.selectById(videoId);
                    if (video == null || video.getIsDeleted() == 1) {
                        return null; // 过滤已删除的视频
                    }
                    return convertToVideoVO(video);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 构建分页响应
        return PageVO.<VideoVO>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(videoIdPage.getTotal())
                .pages((int) videoIdPage.getPages())
                .records(videoVOList)
                .build();
    }

    @Override
    public Boolean isLiked(Long userId, Long videoId) {
        UserLike userLike = userLikeMapper.selectByUserIdAndVideoId(userId, videoId);
        return userLike != null;
    }

    /**
     * 验证分页参数
     */
    private void validatePaginationParams(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            throw new com.example.shortmovie.exception.ValidationException("页码必须大于等于1");
        }
        if (pageSize == null || pageSize <= 0) {
            throw new com.example.shortmovie.exception.ValidationException("每页大小必须大于0");
        }
    }

    /**
     * 转换为 VideoVO
     */
    private VideoVO convertToVideoVO(Video video) {
        // 获取视频 URL（如果存储服务可用）
        String videoUrl = null;
        if (fileStorageService != null && video.getObjectKey() != null) {
            videoUrl = fileStorageService.getFileUrl(video.getObjectKey());
        }

        return VideoVO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .authorName(video.getAuthorName())
                .coverUrl(video.getCoverUrl())
                .videoUrl(videoUrl)
                .duration(video.getDuration())
                .playCount(video.getPlayCount())
                .likeCount(video.getLikeCount())
                .commentCount(video.getCommentCount())
                .collectCount(video.getCollectCount())
                .isLiked(true) // 在点赞列表中，所有视频都是已点赞状态
                .isCollected(false) // 这里不查询收藏状态，避免额外查询
                .createTime(video.getCreateTime())
                .build();
    }
}
