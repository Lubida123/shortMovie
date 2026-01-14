package com.example.shortmovie.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 文件存储服务接口
 */
public interface MinioService {
    
    /**
     * 上传视频文件到 MinIO
     * 
     * @param file 视频文件
     * @return MinIO 对象键（object key）
     */
    String uploadVideo(MultipartFile file);
    
    /**
     * 获取视频访问 URL（带过期时间）
     * 
     * @param objectKey MinIO 对象键
     * @return 预签名访问 URL
     */
    String getVideoUrl(String objectKey);
    
    /**
     * 删除视频文件
     * 
     * @param objectKey MinIO 对象键
     */
    void deleteVideo(String objectKey);
    
    /**
     * 检查 bucket 是否存在，不存在则创建
     */
    void ensureBucketExists();
}
