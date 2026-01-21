package com.example.shortmovie.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.shortmovie.dto.VideoUploadDTO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoDetailVO;
import com.example.shortmovie.vo.VideoUploadVO;
import com.example.shortmovie.vo.VideoVO;

/**
 * 视频服务接口
 */
public interface VideoService {
    
    /**
     * 上传视频
     * 
     * @param file 视频文件
     * @param dto 视频信息
     * @param authorId 作者ID
     * @return 上传结果
     */
    VideoUploadVO uploadVideo(MultipartFile file, VideoUploadDTO dto, Long authorId);
    
    /**
     * 获取视频列表（分页，只返回已审核视频）
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 当前用户ID（可选，用于判断点赞/收藏状态）
     * @return 视频列表
     */
    PageVO<VideoVO> getVideoList(Integer pageNum, Integer pageSize, Long userId);
    
    /**
     * 获取视频详情（包含预签名访问URL）
     * 
     * @param videoId 视频ID
     * @param userId 当前用户ID（可选，用于判断点赞/收藏状态）
     * @return 视频详情
     */
    VideoDetailVO getVideoDetail(Long videoId, Long userId);
    
    /**
     * 增加视频播放次数并发送行为记录到Kafka
     * 
     * @param videoId 视频ID
     * @param userId 用户ID（可为null，表示未登录）
     * @param playDuration 播放时长（秒）
     * @param isCompleted 是否完播
     */
    void incrementPlayCount(Long videoId, Long userId, Integer playDuration, Boolean isCompleted);
}
