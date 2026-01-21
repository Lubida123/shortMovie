package com.example.shortmovie.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.dto.CommentCreateDTO;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.Comment;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.exception.AuthorizationException;
import com.example.shortmovie.exception.BusinessException;
import com.example.shortmovie.exception.DuplicateCommentException;
import com.example.shortmovie.exception.ResourceNotFoundException;
import com.example.shortmovie.exception.ValidationException;
import com.example.shortmovie.mapper.CommentMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.service.CommentService;
import com.example.shortmovie.service.KafkaMessageProducer;
import com.example.shortmovie.service.SensitiveWordService;
import com.example.shortmovie.utils.CommentTreeBuilder;
import com.example.shortmovie.vo.CommentDetailVO;
import com.example.shortmovie.vo.CommentVO;
import com.example.shortmovie.vo.PageVO;

import lombok.RequiredArgsConstructor;

/**
 * 评论服务实现类
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.example.shortmovie.mapper.BehaviorRecordMapper behaviorRecordMapper;
    
    private static final String DUPLICATE_COMMENT_KEY_PREFIX = "comment:duplicate:";
    private static final long DUPLICATE_CHECK_SECONDS = 10;
    
    // Redis缓存相关常量
    private static final String COMMENT_LIST_CACHE_PREFIX = "comment:list:video:";
    private static final long COMMENT_LIST_CACHE_TTL_MINUTES = 30;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long userId, CommentCreateDTO dto) {
        // 验证评论内容
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new ValidationException("评论内容不能为空");
        }
        
        if (dto.getContent().length() > 500) {
            throw new ValidationException("评论内容不能超过500字符");
        }
        
        // 检查敏感词
        if (sensitiveWordService.containsSensitiveWord(dto.getContent())) {
            throw new ValidationException("评论内容包含敏感词，请修改后重试");
        }
        
        // 检测重复评论 - 使用Redis分布式锁防止重复提交
        String commentHash = generateCommentHash(userId, dto.getVideoId(), dto.getContent());
        String redisKey = DUPLICATE_COMMENT_KEY_PREFIX + userId;
        
        // 尝试设置Redis键，如果键已存在则返回false
        Boolean isNewComment = redisTemplate.opsForValue().setIfAbsent(
            redisKey, 
            commentHash, 
            DUPLICATE_CHECK_SECONDS, 
            TimeUnit.SECONDS
        );
        
        if (Boolean.FALSE.equals(isNewComment)) {
            // Key已存在，检查是否是相同内容
            String existingHash = (String) redisTemplate.opsForValue().get(redisKey);
            if (commentHash.equals(existingHash)) {
                // 相同内容的重复评论，拒绝
                throw new DuplicateCommentException("请勿重复提交相同评论");
            }
            // 如果内容不同，更新hash值（允许用户在短时间内发表不同内容的评论）
            redisTemplate.opsForValue().set(redisKey, commentHash, DUPLICATE_CHECK_SECONDS, TimeUnit.SECONDS);
        }
        
        // 验证视频是否存在
        Video video = videoMapper.selectById(dto.getVideoId());
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }
        
        // 如果是回复评论，验证父评论是否存在
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment == null || parentComment.getIsDeleted() == 1) {
                throw new ResourceNotFoundException("父评论不存在或已被删除");
            }
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setVideoId(dto.getVideoId());
        // 设置父评论ID，null或0表示一级评论
        Long parentId = 0L;
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            parentId = dto.getParentId();
        }
        comment.setParentId(parentId);
        comment.setContent(dto.getContent());
        comment.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        comment.setUserAvatar(user.getAvatar());
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        
        // 在同一事务中更新视频评论计数（原子操作）
        int updateResult = videoMapper.incrementCommentCount(dto.getVideoId());
        if (updateResult == 0) {
            // 更新失败可能是因为视频已被删除或并发冲突
            throw new BusinessException("更新视频评论计数失败，视频可能已被删除");
        }
        
        // 删除该视频的评论列表缓存
        invalidateCommentListCache(dto.getVideoId());
        
        // 异步发送Kafka消息
        try {
            BehaviorRecord behaviorRecord = new BehaviorRecord();
            behaviorRecord.setUserId(userId);
            behaviorRecord.setVideoId(dto.getVideoId());
            behaviorRecord.setBehaviorType("COMMENT");
            behaviorRecord.setCreateTime(LocalDateTime.now());
            
            // 1. 先保存到MySQL（持久化）
            behaviorRecordMapper.insert(behaviorRecord);
            
            // 2. 再发送到Kafka
            kafkaMessageProducer.sendBehaviorObject(behaviorRecord);
        } catch (Exception e) {
            // Kafka发送失败不影响主流程，记录日志即可
            // 实际生产环境应该使用日志框架
            System.err.println("发送Kafka消息失败: " + e.getMessage());
        }
        
        // 转换为VO返回
        return convertToVO(comment);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        // 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new ResourceNotFoundException("评论不存在或已被删除");
        }
        
        // 验证用户权限
        if (!comment.getUserId().equals(userId)) {
            throw new AuthorizationException("无权删除他人评论");
        }
        
        // 收集要删除的评论ID列表
        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(commentId);
        
        // 如果是父评论，查询所有子回复
        if (comment.getParentId() == 0) {
            List<Comment> replies = commentMapper.selectRepliesByParentId(commentId);
            if (replies != null && !replies.isEmpty()) {
                idsToDelete.addAll(replies.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList()));
            }
        }
        
        // 批量逻辑删除
        commentMapper.batchDeleteByIds(idsToDelete);
        
        // 在同一事务中更新视频评论计数（原子操作）
        int deleteCount = idsToDelete.size();
        int updateResult = videoMapper.decrementCommentCountByAmount(comment.getVideoId(), deleteCount);
        if (updateResult == 0) {
            // 更新失败可能是因为视频已被删除或并发冲突
            throw new BusinessException("更新视频评论计数失败，视频可能已被删除");
        }
        
        // 删除该视频的评论列表缓存
        invalidateCommentListCache(comment.getVideoId());
    }
    
    @Override
    public PageVO<CommentVO> getVideoComments(Long videoId, Integer pageNum, Integer pageSize) {
        // 验证视频是否存在
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("视频不存在");
        }
        
        // 尝试从Redis缓存获取评论列表
        String cacheKey = buildCommentListCacheKey(videoId, pageNum, pageSize);
        
        try {
            @SuppressWarnings("unchecked")
            PageVO<CommentVO> cachedResult = (PageVO<CommentVO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }
        } catch (Exception e) {
            // 缓存读取失败，继续从数据库查询
            System.err.println("从Redis读取评论列表缓存失败: " + e.getMessage());
        }
        
        // 缓存未命中，从数据库查询
        // 分页查询一级评论
        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> commentPage = commentMapper.selectParentCommentsByVideoId(page, videoId);
        
        // 收集所有评论（包括一级评论和回复）
        List<Comment> allComments = new ArrayList<>(commentPage.getRecords());
        
        // 批量查询所有一级评论的回复，避免N+1问题
        for (Comment parentComment : commentPage.getRecords()) {
            List<Comment> replies = commentMapper.selectRepliesByParentId(parentComment.getId());
            if (replies != null && !replies.isEmpty()) {
                allComments.addAll(replies);
            }
        }
        
        // 使用CommentTreeBuilder构建树形结构
        List<CommentVO> commentVOList = CommentTreeBuilder.buildCommentTree(allComments);
        
        // 构建分页VO
        PageVO<CommentVO> result = PageVO.<CommentVO>builder()
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(commentPage.getTotal())
            .pages((int) commentPage.getPages())
            .records(commentVOList)
            .build();
        
        // 将结果写入Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, result, COMMENT_LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            // 缓存写入失败不影响主流程
            System.err.println("写入评论列表缓存失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public PageVO<CommentDetailVO> getUserComments(Long userId, Integer pageNum, Integer pageSize) {
        // 分页查询用户评论
        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> commentPage = commentMapper.selectCommentsByUserId(page, userId);
        
        // 转换为CommentDetailVO
        List<CommentDetailVO> commentDetailVOList = commentPage.getRecords().stream()
            .map(comment -> {
                // 查询视频信息
                Video video = videoMapper.selectById(comment.getVideoId());
                String videoTitle = video != null ? video.getTitle() : "视频已删除";
                
                return CommentDetailVO.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .videoId(comment.getVideoId())
                    .videoTitle(videoTitle)
                    .content(comment.getContent())
                    .userName(comment.getUserName())
                    .createTime(comment.getCreateTime())
                    .build();
            })
            .collect(Collectors.toList());
        
        // 构建分页VO
        return PageVO.<CommentDetailVO>builder()
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(commentPage.getTotal())
            .pages((int) commentPage.getPages())
            .records(commentDetailVOList)
            .build();
    }
    
    @Override
    public CommentVO getCommentDetail(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new ResourceNotFoundException("评论不存在或已被删除");
        }
        
        // 收集评论和回复
        List<Comment> allComments = new ArrayList<>();
        allComments.add(comment);
        
        // 如果是父评论，查询回复列表
        if (comment.getParentId() == 0) {
            List<Comment> replies = commentMapper.selectRepliesByParentId(commentId);
            if (replies != null && !replies.isEmpty()) {
                allComments.addAll(replies);
            }
        }
        
        // 使用CommentTreeBuilder构建树形结构
        List<CommentVO> commentVOList = CommentTreeBuilder.buildCommentTree(allComments);
        
        // 返回第一个元素（即请求的评论）
        return commentVOList.isEmpty() ? null : commentVOList.get(0);
    }
    
    /**
     * 将Comment实体转换为CommentVO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setVideoId(comment.getVideoId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setUserName(comment.getUserName());
        vo.setUserAvatar(comment.getUserAvatar());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());
        vo.setIsLiked(false); // 默认未点赞，后续可扩展
        return vo;
    }
    
    /**
     * 生成评论内容的hash值，用于重复检测
     */
    private String generateCommentHash(Long userId, Long videoId, String content) {
        try {
            String input = userId + ":" + videoId + ":" + content;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5算法不存在的情况极少，使用简单的hash作为降级方案
            return String.valueOf((userId + ":" + videoId + ":" + content).hashCode());
        }
    }
    
    /**
     * 构建评论列表缓存key
     */
    private String buildCommentListCacheKey(Long videoId, Integer pageNum, Integer pageSize) {
        return COMMENT_LIST_CACHE_PREFIX + videoId + ":page:" + pageNum + ":size:" + pageSize;
    }
    
    /**
     * 删除指定视频的所有评论列表缓存
     */
    private void invalidateCommentListCache(Long videoId) {
        try {
            // 使用模式匹配删除该视频的所有分页缓存
            String pattern = COMMENT_LIST_CACHE_PREFIX + videoId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // 缓存删除失败不影响主流程
            System.err.println("删除评论列表缓存失败: " + e.getMessage());
        }
    }
}
