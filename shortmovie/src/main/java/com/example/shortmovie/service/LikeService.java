package com.example.shortmovie.service;

import com.example.shortmovie.vo.LikeStatusVO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

/**
 * 点赞服务接口
 */
public interface LikeService {

    /**
     * 切换点赞状态（点赞/取消点赞）
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 点赞状态响应
     */
    LikeStatusVO toggleLike(Long userId, Long videoId);

    /**
     * 查询用户点赞列表（我的喜欢）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 点赞视频分页列表
     */
    PageVO<VideoVO> getUserLikeList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户是否点赞某视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 是否已点赞
     */
    Boolean isLiked(Long userId, Long videoId);
}
