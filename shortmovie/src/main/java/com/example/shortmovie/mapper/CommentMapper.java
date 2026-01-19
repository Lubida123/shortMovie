package com.example.shortmovie.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.Comment;

/**
 * 评论 Mapper 接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 查询视频的一级评论列表（分页）
     * 
     * @param page 分页对象
     * @param videoId 视频ID
     * @return 一级评论分页列表
     */
    @Select("SELECT * FROM comment WHERE video_id = #{videoId} AND parent_id = 0 AND is_deleted = 0 ORDER BY create_time DESC")
    IPage<Comment> selectParentCommentsByVideoId(Page<Comment> page, @Param("videoId") Long videoId);
    
    /**
     * 查询某条评论的所有回复
     * 
     * @param parentId 父评论ID
     * @return 回复列表
     */
    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY create_time ASC")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);
    
    /**
     * 查询用户的评论历史（分页）
     * 
     * @param page 分页对象
     * @param userId 用户ID
     * @return 用户评论分页列表
     */
    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    IPage<Comment> selectCommentsByUserId(Page<Comment> page, @Param("userId") Long userId);
    
    /**
     * 批量删除评论（逻辑删除）
     * 
     * @param ids 评论ID列表
     * @return 影响的行数
     */
    @Update("<script>" +
            "UPDATE comment SET is_deleted = 1 WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 统计视频的评论总数
     * 
     * @param videoId 视频ID
     * @return 评论总数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE video_id = #{videoId} AND is_deleted = 0")
    Long countByVideoId(@Param("videoId") Long videoId);
}
