package com.example.shortmovie.service;

import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoVO;

/**
 * 用户视频服务接口
 */
public interface UserVideoService {
    
    /**
     * 查询用户发布的所有视频（我的视频）
     * 
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    PageVO<VideoVO> getUserVideoList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询用户发布的视频列表（按审核状态筛选）
     * 
     * @param userId 用户ID
     * @param auditStatus 审核状态：0-待审核，1-通过，2-驳回
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    PageVO<VideoVO> getUserVideoListByStatus(Long userId, Integer auditStatus, 
                                             Integer pageNum, Integer pageSize);
}
