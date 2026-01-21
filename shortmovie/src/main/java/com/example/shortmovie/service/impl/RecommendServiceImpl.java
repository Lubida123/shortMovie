package com.example.shortmovie.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.RecommendationResult;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.mapper.BehaviorRecordMapper;
import com.example.shortmovie.mapper.RecommendationResultMapper;
import com.example.shortmovie.mapper.UserCollectMapper;
import com.example.shortmovie.mapper.UserLikeMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.FileStorageService;
import com.example.shortmovie.service.RecommendService;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 推荐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendationResultMapper recommendationResultMapper;
    private final VideoMapper videoMapper;
    private final UserLikeMapper userLikeMapper;
    private final UserCollectMapper userCollectMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    
    @Autowired(required = false)
    private FileStorageService fileStorageService;
    
    // Redis Key前缀
    private static final String RECOMMEND_USER_PREFIX = "recommend:user:";
    
    @Override
    public List<VideoVO> getRealtimeRecommendFromRedis(Long userId) {
        try {
            // 从Redis读取推荐视频ID列表
            String redisKey = RECOMMEND_USER_PREFIX + userId;
            List<Object> videoIdObjects = redisTemplate.opsForList().range(redisKey, 0, -1);
            
            if (videoIdObjects == null || videoIdObjects.isEmpty()) {
                log.debug("No realtime recommendations found in Redis for userId={}", userId);
                return Collections.emptyList();
            }
            
            // 转换为Long类型的视频ID列表
            List<Long> videoIds = videoIdObjects.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        } else if (obj instanceof String) {
                            return Long.parseLong((String) obj);
                        }
                        return null;
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            
            if (videoIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 查询视频详情
            List<Video> videos = videoMapper.selectBatchIds(videoIds);
            
            if (videos == null || videos.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 获取用户的点赞和收藏状态
            Set<Long> likedVideoIds = getUserLikedVideoIds(userId);
            Set<Long> collectedVideoIds = getUserCollectedVideoIds(userId);
            
            // 转换为VideoVO并保持推荐顺序
            List<VideoVO> result = new ArrayList<>();
            for (Long videoId : videoIds) {
                videos.stream()
                        .filter(video -> video.getId().equals(videoId))
                        .findFirst()
                        .ifPresent(video -> result.add(convertToVideoVO(video, likedVideoIds, collectedVideoIds)));
            }
            
            log.info("Retrieved {} realtime recommendations from Redis for userId={}", result.size(), userId);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to get realtime recommendations from Redis for userId={}: {}", userId, e.getMessage(), e);
            // Redis故障时返回空列表，由调用方决定是否降级
            return Collections.emptyList();
        }
    }
    
    @Override
    public PageVO<VideoVO> getOfflineRecommendFromDB(Long userId, Integer pageNum, Integer pageSize) {
        try {
            // 创建分页对象
            Page<RecommendationResult> page = new Page<>(pageNum, pageSize);
            
            // 查询离线推荐结果（type=OFFLINE，按rank升序）
            LambdaQueryWrapper<RecommendationResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RecommendationResult::getUserId, userId)
                    .eq(RecommendationResult::getType, "OFFLINE")
                    .orderByAsc(RecommendationResult::getRank);
            
            Page<RecommendationResult> resultPage = recommendationResultMapper.selectPage(page, queryWrapper);
            
            if (resultPage.getRecords().isEmpty()) {
                log.debug("No offline recommendations found for userId={}, fallback to hot videos", userId);
                // 冷启动策略：离线推荐为空时，降级返回热门视频
                return getHotVideosAsPage(pageNum, pageSize);
            }
            
            // 提取视频ID列表
            List<Long> videoIds = resultPage.getRecords().stream()
                    .map(RecommendationResult::getMovieId)
                    .collect(Collectors.toList());
            
            // 查询视频详情
            List<Video> videos = videoMapper.selectBatchIds(videoIds);
            
            if (videos == null || videos.isEmpty()) {
                // 视频已被删除，降级返回热门视频
                log.warn("Recommended videos not found for userId={}, fallback to hot videos", userId);
                return getHotVideosAsPage(pageNum, pageSize);
            }
            
            // 获取用户的点赞和收藏状态
            Set<Long> likedVideoIds = getUserLikedVideoIds(userId);
            Set<Long> collectedVideoIds = getUserCollectedVideoIds(userId);
            
            // 转换为VideoVO并保持推荐顺序（按rank排序）
            List<VideoVO> videoVOList = new ArrayList<>();
            for (Long videoId : videoIds) {
                videos.stream()
                        .filter(video -> video.getId().equals(videoId))
                        .findFirst()
                        .ifPresent(video -> videoVOList.add(convertToVideoVO(video, likedVideoIds, collectedVideoIds)));
            }
            
            log.info("Retrieved {} offline recommendations from DB for userId={}", videoVOList.size(), userId);
            
            return PageVO.<VideoVO>builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .total(resultPage.getTotal())
                    .pages((int) resultPage.getPages())
                    .records(videoVOList)
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to get offline recommendations from DB for userId={}: {}", userId, e.getMessage(), e);
            // 异常时降级返回热门视频
            return getHotVideosAsPage(pageNum, pageSize);
        }
    }
    
    @Override
    public PageVO<VideoVO> getHybridRecommend(Long userId, Integer pageNum, Integer pageSize) {
        try {
            // 冷启动策略1：检查是否为新用户
            if (isNewUser(userId)) {
                log.info("New user detected (userId={}), returning hot videos", userId);
                return getHotVideosAsPage(pageNum, pageSize);
            }
            
            // 1. 优先获取实时推荐
            List<VideoVO> realtimeRecommendations = getRealtimeRecommendFromRedis(userId);
            
            // 2. 如果实时推荐足够，直接返回
            if (realtimeRecommendations.size() >= pageSize) {
                int fromIndex = (pageNum - 1) * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, realtimeRecommendations.size());
                
                if (fromIndex >= realtimeRecommendations.size()) {
                    // 超出范围，返回空列表
                    return PageVO.<VideoVO>builder()
                            .pageNum(pageNum)
                            .pageSize(pageSize)
                            .total((long) realtimeRecommendations.size())
                            .pages((int) Math.ceil((double) realtimeRecommendations.size() / pageSize))
                            .records(Collections.emptyList())
                            .build();
                }
                
                List<VideoVO> pageRecords = realtimeRecommendations.subList(fromIndex, toIndex);
                
                return PageVO.<VideoVO>builder()
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .total((long) realtimeRecommendations.size())
                        .pages((int) Math.ceil((double) realtimeRecommendations.size() / pageSize))
                        .records(pageRecords)
                        .build();
            }
            
            // 3. 冷启动策略2：实时推荐不足，降级使用离线推荐
            log.debug("Realtime recommendations insufficient ({}), fallback to offline recommendations for userId={}", 
                    realtimeRecommendations.size(), userId);
            
            // 获取离线推荐（离线推荐内部已实现降级到热门视频）
            PageVO<VideoVO> offlineRecommendations = getOfflineRecommendFromDB(userId, pageNum, pageSize);
            
            // 4. 合并推荐结果（实时推荐在前）
            List<VideoVO> hybridList = new ArrayList<>(realtimeRecommendations);
            
            // 过滤掉已经在实时推荐中的视频
            Set<Long> realtimeVideoIds = realtimeRecommendations.stream()
                    .map(VideoVO::getId)
                    .collect(Collectors.toSet());
            
            List<VideoVO> filteredOfflineRecommendations = offlineRecommendations.getRecords().stream()
                    .filter(video -> !realtimeVideoIds.contains(video.getId()))
                    .collect(Collectors.toList());
            
            hybridList.addAll(filteredOfflineRecommendations);
            
            // 5. 分页处理
            int fromIndex = (pageNum - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, hybridList.size());
            
            if (fromIndex >= hybridList.size()) {
                return PageVO.<VideoVO>builder()
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .total((long) hybridList.size())
                        .pages((int) Math.ceil((double) hybridList.size() / pageSize))
                        .records(Collections.emptyList())
                        .build();
            }
            
            List<VideoVO> pageRecords = hybridList.subList(fromIndex, toIndex);
            
            log.info("Retrieved {} hybrid recommendations for userId={} (realtime: {}, offline: {})", 
                    pageRecords.size(), userId, realtimeRecommendations.size(), filteredOfflineRecommendations.size());
            
            return PageVO.<VideoVO>builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .total((long) hybridList.size())
                    .pages((int) Math.ceil((double) hybridList.size() / pageSize))
                    .records(pageRecords)
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to get hybrid recommendations for userId={}: {}", userId, e.getMessage(), e);
            // 降级返回热门视频
            return getHotVideosAsPage(pageNum, pageSize);
        }
    }
    
    @Override
    public List<VideoVO> getHotVideos(Integer limit) {
        try {
            // 查询热门视频（优先按热度分数降序，其次按播放量降序）
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Video::getAuditStatus, 1)  // 只查询已审核的视频
                    .orderByDesc(Video::getHeatScore)   // 优先按热度分数排序
                    .orderByDesc(Video::getPlayCount)   // 热度分数相同时按播放量排序
                    .last("LIMIT " + limit);
            
            List<Video> hotVideos = videoMapper.selectList(queryWrapper);
            
            if (hotVideos == null || hotVideos.isEmpty()) {
                log.warn("No hot videos found");
                return Collections.emptyList();
            }
            
            // 转换为VideoVO（热门视频不需要用户特定的点赞/收藏状态）
            List<VideoVO> result = hotVideos.stream()
                    .map(video -> convertToVideoVO(video, null, null))
                    .collect(Collectors.toList());
            
            log.info("Retrieved {} hot videos (sorted by heatScore)", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("Failed to get hot videos: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean isNewUser(Long userId) {
        try {
            // 检查用户是否有历史行为记录
            LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(BehaviorRecord::getUserId, userId)
                    .last("LIMIT 1");
            
            Long count = behaviorRecordMapper.selectCount(queryWrapper);
            boolean isNew = count == null || count == 0;
            
            if (isNew) {
                log.info("User {} is a new user with no behavior history", userId);
            }
            
            return isNew;
            
        } catch (Exception e) {
            log.error("Failed to check if user is new for userId={}: {}", userId, e.getMessage(), e);
            // 异常时假设不是新用户，避免过度降级
            return false;
        }
    }
    
    @Override
    public List<VideoVO> getCategoryBasedRecommendations(Long userId, Integer limit) {
        try {
            // 1. 获取用户历史交互的视频
            LambdaQueryWrapper<BehaviorRecord> behaviorQuery = new LambdaQueryWrapper<>();
            behaviorQuery.eq(BehaviorRecord::getUserId, userId)
                    .select(BehaviorRecord::getVideoId)
                    .last("LIMIT 50");  // 最近50条行为
            
            List<BehaviorRecord> behaviors = behaviorRecordMapper.selectList(behaviorQuery);
            
            if (behaviors.isEmpty()) {
                log.debug("No behavior history for userId={}, returning hot videos", userId);
                return getHotVideos(limit);
            }
            
            // 2. 获取用户交互过的视频详情
            List<Long> interactedVideoIds = behaviors.stream()
                    .map(BehaviorRecord::getVideoId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Video> interactedVideos = videoMapper.selectBatchIds(interactedVideoIds);
            
            if (interactedVideos.isEmpty()) {
                return getHotVideos(limit);
            }
            
            // 3. 统计用户最常交互的分类和标签
            Map<String, Long> categoryCount = interactedVideos.stream()
                    .filter(v -> v.getCategory() != null && !v.getCategory().isEmpty())
                    .collect(Collectors.groupingBy(Video::getCategory, Collectors.counting()));
            
            Map<String, Long> tagCount = new HashMap<>();
            for (Video video : interactedVideos) {
                if (video.getTags() != null && !video.getTags().isEmpty()) {
                    String[] tags = video.getTags().split(",");
                    for (String tag : tags) {
                        String trimmedTag = tag.trim();
                        if (!trimmedTag.isEmpty()) {
                            tagCount.merge(trimmedTag, 1L, Long::sum);
                        }
                    }
                }
            }
            
            // 4. 找出最热门的分类和标签
            String topCategory = categoryCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            
            List<String> topTags = tagCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            // 5. 基于分类和标签推荐新视频
            LambdaQueryWrapper<Video> recommendQuery = new LambdaQueryWrapper<>();
            recommendQuery.eq(Video::getAuditStatus, 1)
                    .notIn(!interactedVideoIds.isEmpty(), Video::getId, interactedVideoIds);  // 排除已交互视频
            
            // 优先匹配分类
            if (topCategory != null) {
                recommendQuery.eq(Video::getCategory, topCategory);
            }
            
            // 如果有标签，添加标签匹配条件
            if (!topTags.isEmpty()) {
                recommendQuery.and(wrapper -> {
                    for (int i = 0; i < topTags.size(); i++) {
                        if (i == 0) {
                            wrapper.like(Video::getTags, topTags.get(i));
                        } else {
                            wrapper.or().like(Video::getTags, topTags.get(i));
                        }
                    }
                });
            }
            
            recommendQuery.orderByDesc(Video::getPlayCount)
                    .last("LIMIT " + limit);
            
            List<Video> recommendedVideos = videoMapper.selectList(recommendQuery);
            
            // 6. 如果基于分类标签的推荐不足，补充热门视频
            if (recommendedVideos.size() < limit) {
                log.debug("Category-based recommendations insufficient ({}), supplementing with hot videos", recommendedVideos.size());
                
                Set<Long> existingIds = recommendedVideos.stream()
                        .map(Video::getId)
                        .collect(Collectors.toSet());
                existingIds.addAll(interactedVideoIds);
                
                LambdaQueryWrapper<Video> hotQuery = new LambdaQueryWrapper<>();
                hotQuery.eq(Video::getAuditStatus, 1)
                        .notIn(!existingIds.isEmpty(), Video::getId, existingIds)
                        .orderByDesc(Video::getPlayCount)
                        .last("LIMIT " + (limit - recommendedVideos.size()));
                
                List<Video> hotVideos = videoMapper.selectList(hotQuery);
                recommendedVideos.addAll(hotVideos);
            }
            
            // 7. 转换为VideoVO
            Set<Long> likedVideoIds = getUserLikedVideoIds(userId);
            Set<Long> collectedVideoIds = getUserCollectedVideoIds(userId);
            
            List<VideoVO> result = recommendedVideos.stream()
                    .map(video -> convertToVideoVO(video, likedVideoIds, collectedVideoIds))
                    .collect(Collectors.toList());
            
            log.info("Retrieved {} category-based recommendations for userId={} (category: {}, tags: {})", 
                    result.size(), userId, topCategory, topTags);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to get category-based recommendations for userId={}: {}", userId, e.getMessage(), e);
            // 异常时降级返回热门视频
            return getHotVideos(limit);
        }
    }
    
    /**
     * 获取热门视频作为分页结果（降级使用）
     */
    private PageVO<VideoVO> getHotVideosAsPage(Integer pageNum, Integer pageSize) {
        try {
            // 创建分页对象
            Page<Video> page = new Page<>(pageNum, pageSize);
            
            // 查询热门视频（优先按热度分数降序，其次按播放量降序）
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Video::getAuditStatus, 1)
                    .orderByDesc(Video::getHeatScore)    // 优先按热度分数排序
                    .orderByDesc(Video::getPlayCount);   // 热度分数相同时按播放量排序
            
            Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
            
            // 转换为VideoVO
            List<VideoVO> videoVOList = videoPage.getRecords().stream()
                    .map(video -> convertToVideoVO(video, null, null))
                    .collect(Collectors.toList());
            
            log.info("Fallback to hot videos: retrieved {} videos (sorted by heatScore)", videoVOList.size());
            
            return PageVO.<VideoVO>builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .total(videoPage.getTotal())
                    .pages((int) videoPage.getPages())
                    .records(videoVOList)
                    .build();
            
        } catch (Exception e) {
            log.error("Failed to get hot videos as page: {}", e.getMessage(), e);
            return PageVO.<VideoVO>builder()
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .total(0L)
                    .pages(0)
                    .records(Collections.emptyList())
                    .build();
        }
    }
    
    /**
     * 转换为VideoVO
     */
    private VideoVO convertToVideoVO(Video video, Set<Long> likedVideoIds, Set<Long> collectedVideoIds) {
        // 获取视频URL（如果存储服务可用）
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
                .isLiked(likedVideoIds != null && likedVideoIds.contains(video.getId()))
                .isCollected(collectedVideoIds != null && collectedVideoIds.contains(video.getId()))
                .createTime(video.getCreateTime())
                .build();
    }
    
    /**
     * 获取用户点赞的视频ID集合
     */
    private Set<Long> getUserLikedVideoIds(Long userId) {
        List<Long> videoIds = userLikeMapper.selectAllVideoIdsByUserId(userId);
        return videoIds.stream().collect(Collectors.toSet());
    }
    
    /**
     * 获取用户收藏的视频ID集合
     */
    private Set<Long> getUserCollectedVideoIds(Long userId) {
        List<Long> videoIds = userCollectMapper.selectAllVideoIdsByUserId(userId);
        return videoIds.stream().collect(Collectors.toSet());
    }
}
