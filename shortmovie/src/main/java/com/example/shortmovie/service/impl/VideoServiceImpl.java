package com.example.shortmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.dto.VideoUploadDTO;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.exception.BusinessException;
import com.example.shortmovie.exception.ResourceNotFoundException;
import com.example.shortmovie.mapper.BehaviorRecordMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.MinioService;
import com.example.shortmovie.service.VideoService;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoDetailVO;
import com.example.shortmovie.vo.VideoUploadVO;
import com.example.shortmovie.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;

    // 使用 Optional 处理可能不存在的 MinioService
    @Autowired(required = false)
    private MinioService minioService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoUploadVO uploadVideo(MultipartFile file, VideoUploadDTO dto, Long authorId) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }

        // 检查 MinIO 是否可用
        if (minioService == null) {
            throw new BusinessException(500, "文件存储服务未启用，请联系管理员");
        }

        // 验证作者存在
        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 上传文件到 MinIO
        String objectKey = minioService.uploadVideo(file);

        // 创建视频记录
        Video video = new Video();
        video.setTitle(dto.getTitle());
        video.setDescription(dto.getDescription());
        video.setAuthorId(authorId);
        video.setAuthorName(author.getUsername());
        video.setObjectKey(objectKey);
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

        // 获取视频访问 URL
        String videoUrl = minioService.getVideoUrl(objectKey);

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
        return PageVO.<VideoVO>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(videoPage.getTotal())
                .pages((int) videoPage.getPages())
                .records(videoVOList)
                .build();
    }

    @Override
    public VideoDetailVO getVideoDetail(Long videoId, Long userId) {
        // 查询视频
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }

        // 获取 MinIO 预签名 URL（如果 MinIO 可用）
        String videoUrl = null;
        if (minioService != null && video.getObjectKey() != null) {
            videoUrl = minioService.getVideoUrl(video.getObjectKey());
        }

        // 如果用户已登录，查询用户的点赞和收藏状态
        Boolean isLiked = false;
        Boolean isCollected = false;

        if (userId != null) {
            isLiked = checkUserLiked(userId, videoId);
            isCollected = checkUserCollected(userId, videoId);
        }

        // 转换为 VO
        return VideoDetailVO.builder()
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
                .isLiked(isLiked)
                .isCollected(isCollected)
                .createTime(video.getCreateTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementPlayCount(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }

        // 增加播放次数
        video.setPlayCount(video.getPlayCount() + 1);
        videoMapper.updateById(video);
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
        // 获取视频 URL（如果 MinIO 可用）
        String videoUrl = null;
        if (minioService != null && video.getObjectKey() != null) {
            videoUrl = minioService.getVideoUrl(video.getObjectKey());
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
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getUserId, userId)
                .eq(BehaviorRecord::getBehaviorType, "LIKE");

        List<BehaviorRecord> records = behaviorRecordMapper.selectList(queryWrapper);
        return records.stream()
                .map(BehaviorRecord::getVideoId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户收藏的视频ID集合
     */
    private Set<Long> getUserCollectedVideoIds(Long userId) {
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getUserId, userId)
                .eq(BehaviorRecord::getBehaviorType, "COLLECT");

        List<BehaviorRecord> records = behaviorRecordMapper.selectList(queryWrapper);
        return records.stream()
                .map(BehaviorRecord::getVideoId)
                .collect(Collectors.toSet());
    }

    /**
     * 检查用户是否点赞了视频
     */
    private Boolean checkUserLiked(Long userId, Long videoId) {
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getUserId, userId)
                .eq(BehaviorRecord::getVideoId, videoId)
                .eq(BehaviorRecord::getBehaviorType, "LIKE");

        return behaviorRecordMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查用户是否收藏了视频
     */
    private Boolean checkUserCollected(Long userId, Long videoId) {
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getUserId, userId)
                .eq(BehaviorRecord::getVideoId, videoId)
                .eq(BehaviorRecord::getBehaviorType, "COLLECT");

        return behaviorRecordMapper.selectCount(queryWrapper) > 0;
    }
}

