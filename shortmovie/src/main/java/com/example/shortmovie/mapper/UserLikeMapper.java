package com.example.shortmovie.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.UserLike;

/**
 * 用户点赞 Mapper 接口
 */
@Mapper
public interface UserLikeMapper extends BaseMapper<UserLike> {

    /**
     * 查询用户对视频的点赞记录
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 点赞记录，如果不存在则返回null
     */
    @Select("SELECT * FROM user_like WHERE user_id = #{userId} AND video_id = #{videoId} AND is_deleted = 0")
    UserLike selectByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);

    /**
     * 查询用户点赞的视频ID列表（分页）
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 视频ID分页列表
     */
    @Select("SELECT video_id FROM user_like WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY create_time DESC")
    IPage<Long> selectVideoIdsByUserId(Page<Long> page, @Param("userId") Long userId);

    /**
     * 查询用户点赞的所有视频ID列表（用于批量查询状态）
     *
     * @param userId 用户ID
     * @return 视频ID列表
     */
    @Select("SELECT video_id FROM user_like WHERE user_id = #{userId} AND is_deleted = 0")
    java.util.List<Long> selectAllVideoIdsByUserId(@Param("userId") Long userId);
}
