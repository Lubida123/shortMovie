package com.example.shortmovie.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.dto.VideoUploadDTO;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.exception.BusinessException;
import com.example.shortmovie.exception.ResourceNotFoundException;
import com.example.shortmovie.mapper.UserCollectMapper;
import com.example.shortmovie.mapper.UserLikeMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.FileStorageService;
import com.example.shortmovie.service.InteractionService;
import com.example.shortmovie.service.KafkaMessageProducer;
import com.example.shortmovie.service.VideoService;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoDetailVO;
import com.example.shortmovie.vo.VideoInteractionVO;
import com.example.shortmovie.vo.VideoUploadVO;
import com.example.shortmovie.vo.VideoVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 视频服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper userLikeMapper;
    private final UserCollectMapper userCollectMapper;
    private final InteractionService interactionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final com.example.shortmovie.mapper.BehaviorRecordMapper behaviorRecordMapper;

    // 使用统一的文件存储服务（支持腾讯云 COS 等对象存储）
    @Autowired(required = false)
    private FileStorageService fileStorageService;
    
    // Redis缓存Key前缀
    private static final String VIDEO_DETAIL_CACHE_PREFIX = "video:detail:";
    private static final String VIDEO_LIST_CACHE_PREFIX = "video:list:";
    
    // 缓存过期时间
    private static final long VIDEO_DETAIL_CACHE_TTL_MINUTES = 60; // 视频详情缓存1小时
    private static final long VIDEO_LIST_CACHE_TTL_MINUTES = 10;   // 视频列表缓存10分钟

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoUploadVO uploadVideo(MultipartFile file, VideoUploadDTO dto, Long authorId) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }

        // 检查文件存储服务是否可用
        if (fileStorageService == null) {
            throw new BusinessException(500, "文件存储服务未启用，请联系管理员");
        }

        // 验证作者存在
        User author = userMapper.selectById(authorId);
        if (author == null) {
            // 临时处理：如果用户不存在，创建一个默认用户信息（仅用于测试）
            author = new User();
            author.setId(authorId);
            author.setUsername("测试用户");
        }

        // 上传文件到存储服务
        String objectKey = fileStorageService.uploadFile(file);

        // 获取视频访问 URL（仅用于返回给前端，不存储到数据库）
        String videoUrl = fileStorageService.getFileUrl(objectKey);

        // 创建视频记录
        Video video = new Video();
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setAuthorId(authorId);
        video.setAuthorName(author.getUsername());
        video.setObjectKey(objectKey);
        // 不再存储临时URL到数据库，只存储objectKey
        // video.setVideoUrl(videoUrl);  // 已弃用：临时URL会过期
        video.setFileSize(file.getSize());
        video.setFormat(getFileExtension(file.getOriginalFilename()));
        video.setCategory(dto.getCategory());
        video.setTags(dto.getTags());
        video.setDuration(dto.getDuration());
        video.setAuditStatus(0); // 待审核
        video.setIsHot(0);
        video.setPlayCount(0L);
        video.setLikeCount(0L);
        video.setCommentCount(0L);
        video.setCollectCount(0L);
        video.setHeatScore(BigDecimal.ZERO);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());
        video.setIsDeleted(0);

        // 保存到数据库
        videoMapper.insert(video);
        
        // 删除视频列表缓存（新视频发布后）
        invalidateVideoListCache();
        
        log.info("Video uploaded successfully, videoId={}, cleared video list cache", video.getId());

        // 构建响应
        return VideoUploadVO.builder()
                .videoId(video.getId())
                .objectKey(objectKey)
                .videoUrl(videoUrl)
                .fileSize(file.getSize())
                .build();
    }

    @Override
    public PageVO<VideoVO> getVideoList(Integer pageNum, Integer pageSize, Long userId) {
        // 构建缓存Key
        String cacheKey = VIDEO_LIST_CACHE_PREFIX + "page:" + pageNum + ":size:" + pageSize;
        
        // 1. 尝试从Redis缓存读取
        try {
            @SuppressWarnings("unchecked")
            PageVO<VideoVO> cachedResult = (PageVO<VideoVO>) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                log.debug("Video list cache hit: {}", cacheKey);
                
                // 如果用户已登录，需要更新点赞和收藏状态
                if (userId != null) {
                    Set<Long> likedVideoIds = getUserLikedVideoIds(userId);
                    Set<Long> collectedVideoIds = getUserCollectedVideoIds(userId);
                    
                    // 更新每个视频的点赞和收藏状态
                    cachedResult.getRecords().forEach(video -> {
                        video.setIsLiked(likedVideoIds.contains(video.getId()));
                        video.setIsCollected(collectedVideoIds.contains(video.getId()));
                    });
                }
                
                return cachedResult;
            }
        } catch (Exception e) {
            log.warn("Failed to read video list from cache: {}", e.getMessage());
        }
        
        // 2. 缓存未命中，从数据库查询
        log.debug("Video list cache miss: {}, querying database", cacheKey);
        
        // 创建分页对象
        Page<Video> page = new Page<>(pageNum, pageSize);

        // 查询已审核的视频（auditStatus = 1）
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Video::getAuditStatus, 1)
                .orderByDesc(Video::getCreateTime);

        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);

        // 如果用户已登录，查询用户的点赞和收藏记录
        Set<Long> likedVideoIds = null;
        Set<Long> collectedVideoIds = null;

        if (userId != null) {
            likedVideoIds = getUserLikedVideoIds(userId);
            collectedVideoIds = getUserCollectedVideoIds(userId);
        }

        // 转换为 VO
        Set<Long> finalLikedVideoIds = likedVideoIds;
        Set<Long> finalCollectedVideoIds = collectedVideoIds;

        List<VideoVO> videoVOList = videoPage.getRecords().stream()
                .map(video -> convertToVideoVO(video, finalLikedVideoIds, finalCollectedVideoIds))
                .collect(Collectors.toList());

        // 构建分页响应
        PageVO<VideoVO> result = PageVO.<VideoVO>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(videoPage.getTotal())
                .pages((int) videoPage.getPages())
                .records(videoVOList)
                .build();
        
        // 3. 将结果写入Redis缓存（不包含用户特定的点赞/收藏状态）
        try {
            // 创建一个副本用于缓存，将所有视频的isLiked和isCollected设置为false
            PageVO<VideoVO> cacheResult = PageVO.<VideoVO>builder()
                    .pageNum(result.getPageNum())
                    .pageSize(result.getPageSize())
                    .total(result.getTotal())
                    .pages(result.getPages())
                    .records(result.getRecords().stream()
                            .map(video -> {
                                VideoVO cacheVideo = VideoVO.builder()
                                        .id(video.getId())
                                        .title(video.getTitle())
                                        .description(video.getDescription())
                                        .authorName(video.getAuthorName())
                                        .coverUrl(video.getCoverUrl())
                                        .videoUrl(video.getVideoUrl())
                                        .duration(video.getDuration())
                                        .playCount(video.getPlayCount())
                                        .likeCount(video.getLikeCount())
                                        .commentCount(video.getCommentCount())
                                        .collectCount(video.getCollectCount())
                                        .isLiked(false)  // 缓存中不存储用户特定状态
                                        .isCollected(false)  // 缓存中不存储用户特定状态
                                        .createTime(video.getCreateTime())
                                        .build();
                                return cacheVideo;
                            })
                            .collect(Collectors.toList()))
                    .build();
            
            redisTemplate.opsForValue().set(cacheKey, cacheResult, VIDEO_LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Video list cached: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to write video list to cache: {}", e.getMessage());
        }
        
        return result;
    }

    @Override
    public VideoDetailVO getVideoDetail(Long videoId, Long userId) {
        // 构建缓存Key
        String cacheKey = VIDEO_DETAIL_CACHE_PREFIX + videoId;
        
        // 1. 尝试从Redis缓存读取
        VideoDetailVO cachedDetail = null;
        try {
            cachedDetail = (VideoDetailVO) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedDetail != null) {
                log.debug("Video detail cache hit: videoId={}", videoId);
                
                // 如果用户已登录，需要查询用户的点赞和收藏状态
                if (userId != null) {
                    VideoInteractionVO interactionStatus = interactionService.getInteractionStatus(userId, videoId);
                    cachedDetail.setIsLiked(interactionStatus.getIsLiked());
                    cachedDetail.setIsCollected(interactionStatus.getIsCollected());
                } else {
                    cachedDetail.setIsLiked(false);
                    cachedDetail.setIsCollected(false);
                }
                
                return cachedDetail;
            }
        } catch (Exception e) {
            log.warn("Failed to read video detail from cache: videoId={}, error={}", videoId, e.getMessage());
        }
        
        // 2. 缓存未命中，从数据库查询
        log.debug("Video detail cache miss: videoId={}, querying database", videoId);
        
        // 查询视频
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }

        // 获取文件访问 URL（如果存储服务可用）
        String videoUrl = null;
        if (fileStorageService != null && video.getObjectKey() != null) {
            videoUrl = fileStorageService.getFileUrl(video.getObjectKey());
        }

        // 调用InteractionService获取用户的点赞和收藏状态
        VideoInteractionVO interactionStatus = null;
        if (userId != null) {
            interactionStatus = interactionService.getInteractionStatus(userId, videoId);
        }

        // 转换为 VO
        VideoDetailVO result = VideoDetailVO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .authorId(video.getAuthorId())
                .authorName(video.getAuthorName())
                .coverUrl(video.getCoverUrl())
                .videoUrl(videoUrl)
                .objectKey(video.getObjectKey())
                .duration(video.getDuration())
                .fileSize(video.getFileSize())
                .format(video.getFormat())
                .category(video.getCategory())
                .tags(video.getTags())
                .playCount(video.getPlayCount())
                .likeCount(video.getLikeCount())
                .commentCount(video.getCommentCount())
                .collectCount(video.getCollectCount())
                .heatScore(video.getHeatScore())
                .isLiked(interactionStatus != null ? interactionStatus.getIsLiked() : false)
                .isCollected(interactionStatus != null ? interactionStatus.getIsCollected() : false)
                .createTime(video.getCreateTime())
                .build();
        
        // 3. 将结果写入Redis缓存（不包含用户特定的点赞/收藏状态）
        try {
            // 创建一个副本用于缓存，将isLiked和isCollected设置为false
            VideoDetailVO cacheDetail = VideoDetailVO.builder()
                    .id(result.getId())
                    .title(result.getTitle())
                    .description(result.getDescription())
                    .authorId(result.getAuthorId())
                    .authorName(result.getAuthorName())
                    .coverUrl(result.getCoverUrl())
                    .videoUrl(result.getVideoUrl())
                    .objectKey(result.getObjectKey())
                    .duration(result.getDuration())
                    .fileSize(result.getFileSize())
                    .format(result.getFormat())
                    .category(result.getCategory())
                    .tags(result.getTags())
                    .playCount(result.getPlayCount())
                    .likeCount(result.getLikeCount())
                    .commentCount(result.getCommentCount())
                    .collectCount(result.getCollectCount())
                    .heatScore(result.getHeatScore())
                    .isLiked(false)  // 缓存中不存储用户特定状态
                    .isCollected(false)  // 缓存中不存储用户特定状态
                    .createTime(result.getCreateTime())
                    .build();
            
            redisTemplate.opsForValue().set(cacheKey, cacheDetail, VIDEO_DETAIL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Video detail cached: videoId={}", videoId);
        } catch (Exception e) {
            log.warn("Failed to write video detail to cache: videoId={}, error={}", videoId, e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementPlayCount(Long videoId, Long userId, Integer playDuration, Boolean isCompleted) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }

        // 增加播放次数
        video.setPlayCount(video.getPlayCount() + 1);
        videoMapper.updateById(video);
        
        // 删除视频详情缓存（播放数已更新）
        invalidateVideoDetailCache(videoId);
        
        // 异步更新热度分数
        updateHeatScoreAsync(videoId);
        
        log.debug("Video play count incremented and cache invalidated: videoId={}", videoId);
        
        // 如果用户已登录，发送行为记录到Kafka
        if (userId != null) {
            try {
                // 创建行为记录对象
                BehaviorRecord behaviorRecord = new BehaviorRecord();
                behaviorRecord.setUserId(userId);
                behaviorRecord.setVideoId(videoId);
                behaviorRecord.setBehaviorType("PLAY");
                behaviorRecord.setPlayDuration(playDuration);
                behaviorRecord.setIsCompleted((isCompleted != null && isCompleted) ? 1 : 0);
                behaviorRecord.setCreateTime(LocalDateTime.now());
                
                // 1. 先保存到MySQL（持久化）- 使用 INSERT OR UPDATE 避免重复键冲突
                behaviorRecordMapper.saveOrUpdate(behaviorRecord);
                log.debug("Play behavior saved/updated in MySQL: userId={}, videoId={}", userId, videoId);
                
                // 2. 再发送到Kafka（异步，失败不影响主流程）
                kafkaMessageProducer.sendBehaviorObject(behaviorRecord);
                
                log.info("Play behavior sent to Kafka: userId={}, videoId={}, playDuration={}, isCompleted={}", 
                    userId, videoId, playDuration, isCompleted);
            } catch (Exception e) {
                // Kafka发送失败不影响用户操作，仅记录警告日志
                log.warn("Failed to send play behavior to Kafka: userId={}, videoId={}, error={}", 
                    userId, videoId, e.getMessage());
            }
        } else {
            // 未登录用户，跳过Kafka发送
            log.debug("User not logged in, skipping Kafka message for videoId={}", videoId);
        }
    }
    
    /**
     * 删除视频详情缓存
     */
    private void invalidateVideoDetailCache(Long videoId) {
        try {
            String cacheKey = VIDEO_DETAIL_CACHE_PREFIX + videoId;
            redisTemplate.delete(cacheKey);
            log.debug("Video detail cache invalidated: videoId={}", videoId);
        } catch (Exception e) {
            log.warn("Failed to invalidate video detail cache: videoId={}, error={}", videoId, e.getMessage());
        }
    }
    
    /**
     * 删除视频列表缓存
     */
    private void invalidateVideoListCache() {
        try {
            // 使用模式匹配删除所有视频列表缓存
            String pattern = VIDEO_LIST_CACHE_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Video list cache invalidated: {} keys deleted", keys.size());
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate video list cache: {}", e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 转换为 VideoVO
     */
    private VideoVO convertToVideoVO(Video video, Set<Long> likedVideoIds, Set<Long> collectedVideoIds) {
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
    
    /**
     * 异步更新视频热度分数
     * 使用CompletableFuture异步执行，不阻塞主流程
     */
    private void updateHeatScoreAsync(Long videoId) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // 注入HeatScoreService（通过ApplicationContext获取）
                com.example.shortmovie.service.HeatScoreService heatScoreService = 
                    com.example.shortmovie.config.ApplicationContextProvider.getApplicationContext()
                        .getBean(com.example.shortmovie.service.HeatScoreService.class);
                
                heatScoreService.updateVideoHeatScore(videoId);
                log.debug("Heat score updated asynchronously for videoId={}", videoId);
            } catch (Exception e) {
                log.warn("Failed to update heat score asynchronously for videoId={}: {}", videoId, e.getMessage());
            }
        });
    }
    
    @Override
    public PageVO<VideoVO> searchVideos(String keyword, String searchType, String sortBy,
                                        Integer pageNum, Integer pageSize, Long userId) {
        // 1. 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new com.example.shortmovie.exception.ValidationException("搜索关键词不能为空");
        }
        
        // 2. 创建分页对象
        Page<Video> page = new Page<>(pageNum, pageSize);
        
        // 3. 构建查询条件
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询已审核的视频
        queryWrapper.eq(Video::getAuditStatus, 1);
        
        // 根据搜索类型构建查询条件
        String trimmedKeyword = keyword.trim();
        switch (searchType.toLowerCase()) {
            case "title":
                queryWrapper.like(Video::getTitle, trimmedKeyword);
                break;
            case "tag":
                queryWrapper.like(Video::getTags, trimmedKeyword);
                break;
            case "category":
                queryWrapper.eq(Video::getCategory, trimmedKeyword);
                break;
            case "author":
                queryWrapper.like(Video::getAuthorName, trimmedKeyword);
                break;
            case "all":
            default:
                // 搜索多个字段（标题、描述、标签、分类、作者）
                queryWrapper.and(wrapper -> wrapper
                    .like(Video::getTitle, trimmedKeyword)
                    .or().like(Video::getDescription, trimmedKeyword)
                    .or().like(Video::getTags, trimmedKeyword)
                    .or().like(Video::getCategory, trimmedKeyword)
                    .or().like(Video::getAuthorName, trimmedKeyword)
                );
                break;
        }
        
        // 4. 根据排序方式排序
        switch (sortBy.toLowerCase()) {
            case "time":
                queryWrapper.orderByDesc(Video::getCreateTime);
                break;
            case "play":
                queryWrapper.orderByDesc(Video::getPlayCount);
                break;
            case "like":
                queryWrapper.orderByDesc(Video::getLikeCount);
                break;
            case "hot":
            default:
                queryWrapper.orderByDesc(Video::getHeatScore);
                break;
        }
        
        // 5. 执行查询
        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        // 6. 如果用户已登录，查询点赞和收藏状态
        Set<Long> likedVideoIds = null;
        Set<Long> collectedVideoIds = null;
        if (userId != null) {
            likedVideoIds = getUserLikedVideoIds(userId);
            collectedVideoIds = getUserCollectedVideoIds(userId);
        }
        
        // 7. 转换为VO
        Set<Long> finalLikedVideoIds = likedVideoIds;
        Set<Long> finalCollectedVideoIds = collectedVideoIds;
        
        List<VideoVO> videoVOList = videoPage.getRecords().stream()
            .map(video -> convertToVideoVO(video, finalLikedVideoIds, finalCollectedVideoIds))
            .collect(Collectors.toList());
        
        // 8. 构建分页响应
        return PageVO.<VideoVO>builder()
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(videoPage.getTotal())
            .pages((int) videoPage.getPages())
            .records(videoVOList)
            .build();
    }
}

