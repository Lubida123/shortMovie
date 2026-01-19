package com.example.shortmovie.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.FileStorageService;
import com.example.shortmovie.service.UserVideoService;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户视频服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVideoServiceImpl implements UserVideoService {

    private final VideoMapper videoMapper;

    @Autowired(required = false)
    private FileStorageService fileStorageService;

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

    @Override
    public PageVO<VideoVO> getUserVideoList(Long userId, Integer pageNum, Integer pageSize) {
        log.info("查询用户发布的所有视频，userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);
        
        // 验证分页参数
        validatePaginationParams(pageNum, pageSize);
        
        // 创建分页对象
        Page<Video> page = new Page<>(pageNum, pageSize);
        
        // 查询该用户发布的所有视频，按创建时间倒序排列
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Video::getAuthorId, userId)
                .orderByDesc(Video::getCreateTime);
        
        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<VideoVO> videoVOList = videoPage.getRecords().stream()
                .map(this::convertToVideoVO)
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
    public PageVO<VideoVO> getUserVideoListByStatus(Long userId, Integer auditStatus, 
                                                    Integer pageNum, Integer pageSize) {
        log.info("查询用户发布的视频（按审核状态筛选），userId: {}, auditStatus: {}, pageNum: {}, pageSize: {}", 
                userId, auditStatus, pageNum, pageSize);
        
        // 验证分页参数
        validatePaginationParams(pageNum, pageSize);
        
        // 创建分页对象
        Page<Video> page = new Page<>(pageNum, pageSize);
        
        // 查询该用户发布的指定审核状态的视频，按创建时间倒序排列
        LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Video::getAuthorId, userId)
                .eq(Video::getAuditStatus, auditStatus)
                .orderByDesc(Video::getCreateTime);
        
        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<VideoVO> videoVOList = videoPage.getRecords().stream()
                .map(this::convertToVideoVO)
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
                .isLiked(false)  // 我的视频列表不需要显示点赞状态
                .isCollected(false)  // 我的视频列表不需要显示收藏状态
                .createTime(video.getCreateTime())
                .build();
    }
}
