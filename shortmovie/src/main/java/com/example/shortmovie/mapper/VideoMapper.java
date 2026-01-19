package com.example.shortmovie.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.Video;

/**
 * 视频 Mapper 接口
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    
    /**
     * 原子操作：增加点赞计数
     */
    @Update("UPDATE video SET like_count = like_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementLikeCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：减少点赞计数
     */
    @Update("UPDATE video SET like_count = GREATEST(0, like_count - 1) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementLikeCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：增加收藏计数
     */
    @Update("UPDATE video SET collect_count = collect_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementCollectCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：减少收藏计数
     */
    @Update("UPDATE video SET collect_count = GREATEST(0, collect_count - 1) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementCollectCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：增加评论计数
     */
    @Update("UPDATE video SET comment_count = comment_count + 1 WHERE id = #{videoId} AND is_deleted = 0")
    int incrementCommentCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：减少评论计数
     */
    @Update("UPDATE video SET comment_count = GREATEST(0, comment_count - 1) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementCommentCount(@Param("videoId") Long videoId);
    
    /**
     * 原子操作：批量减少评论计数
     */
    @Update("UPDATE video SET comment_count = GREATEST(0, comment_count - #{count}) WHERE id = #{videoId} AND is_deleted = 0")
    int decrementCommentCountByAmount(@Param("videoId") Long videoId, @Param("count") int count);
}
