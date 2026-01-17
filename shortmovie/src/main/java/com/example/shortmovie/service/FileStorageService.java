package com.example.shortmovie.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口（统一对象存储服务，如腾讯云 COS、阿里云 OSS 等）
 */
public interface FileStorageService {
    
    /**
     * 上传文件
     * @param file 文件
     * @return 文件存储路径（objectKey）
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 获取文件访问 URL
     * @param objectKey 文件存储路径
     * @return 访问 URL
     */
    String getFileUrl(String objectKey);
    
    /**
     * 删除文件
     * @param objectKey 文件存储路径
     */
    void deleteFile(String objectKey);
}
