package com.example.shortmovie.service;

import com.example.shortmovie.vo.CollectStatusVO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

/**
 * 收藏服务接口
 */
public interface CollectService {

    /**
     * 切换收藏状态（收藏/取消收藏）
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 收藏状态响应
     */
    CollectStatusVO toggleCollect(Long userId, Long videoId);

    /**
     * 查询用户收藏列表（我的收藏）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 收藏视频分页列表
     */
    PageVO<VideoVO> getUserCollectList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询用户是否收藏某视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 是否已收藏
     */
    Boolean isCollected(Long userId, Long videoId);
}
