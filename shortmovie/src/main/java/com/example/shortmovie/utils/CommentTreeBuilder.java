package com.example.shortmovie.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.shortmovie.entity.Comment;
import com.example.shortmovie.vo.CommentVO;

/**
 * 评论树结构构建工具类
 * 用于将扁平的评论列表组装成树形结构，优化查询性能，避免N+1问题
 */
public class CommentTreeBuilder {
    
    /**
     * 将评论列表构建成树形结构
     * 
     * @param comments 所有评论列表（包括一级评论和回复）
     * @return 树形结构的评论列表（只包含一级评论，回复在replies字段中）
     */
    public static List<CommentVO> buildCommentTree(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 分离一级评论和回复
        List<Comment> parentComments = new ArrayList<>();
        Map<Long, List<Comment>> replyMap = new HashMap<>();
        
        for (Comment comment : comments) {
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                // 一级评论
                parentComments.add(comment);
            } else {
                // 回复评论，按父评论ID分组
                replyMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>())
                    .add(comment);
            }
        }
        
        // 2. 组装树结构
        return parentComments.stream()
            .map(parent -> {
                CommentVO vo = convertToVO(parent);
                
                // 获取该评论的所有回复
                List<Comment> replies = replyMap.get(parent.getId());
                if (replies != null && !replies.isEmpty()) {
                    List<CommentVO> replyVOList = replies.stream()
                        .map(CommentTreeBuilder::convertToVO)
                        .collect(Collectors.toList());
                    vo.setReplies(replyVOList);
                } else {
                    vo.setReplies(new ArrayList<>());
                }
                
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 将Comment实体转换为CommentVO
     * 
     * @param comment 评论实体
     * @return 评论VO
     */
    private static CommentVO convertToVO(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        return CommentVO.builder()
            .id(comment.getId())
            .userId(comment.getUserId())
            .videoId(comment.getVideoId())
            .parentId(comment.getParentId())
            .content(comment.getContent())
            .userName(comment.getUserName())
            .userAvatar(comment.getUserAvatar())
            .likeCount(comment.getLikeCount())
            .createTime(comment.getCreateTime())
            .isLiked(false) // 默认未点赞，后续可扩展
            .replies(new ArrayList<>()) // 初始化空列表
            .build();
    }
}
