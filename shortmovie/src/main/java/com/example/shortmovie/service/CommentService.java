package com.example.shortmovie.service;

import com.example.shortmovie.dto.CommentCreateDTO;
import com.example.shortmovie.vo.CommentDetailVO;
import com.example.shortmovie.vo.CommentVO;
import com.example.shortmovie.vo.PageVO;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 发表评论
     * 
     * @param userId 用户ID
     * @param dto 评论创建DTO
     * @return 评论VO
     */
    CommentVO createComment(Long userId, CommentCreateDTO dto);
    
    /**
     * 删除评论（包括所有子回复）
     * 
     * @param userId 用户ID
     * @param commentId 评论ID
     */
    void deleteComment(Long userId, Long commentId);
    
    /**
     * 查询视频评论列表（分页，包含回复）
     * 
     * @param videoId 视频ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 评论分页VO
     */
    PageVO<CommentVO> getVideoComments(Long videoId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询用户评论历史（分页）
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 评论详情分页VO
     */
    PageVO<CommentDetailVO> getUserComments(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询评论详情
     * 
     * @param commentId 评论ID
     * @return 评论VO
     */
    CommentVO getCommentDetail(Long commentId);
}
