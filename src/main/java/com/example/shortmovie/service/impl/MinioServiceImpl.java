package com.example.shortmovie.service.impl;

import com.example.shortmovie.config.MinioConfig;
import com.example.shortmovie.exception.BusinessException;
import com.example.shortmovie.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
    /**
     * 初始化时确保 bucket 存在
     */
    @PostConstruct
    public void init() {
        ensureBucketExists();
    }
    
    @Override
    public String uploadVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        
        try {
            // 生成唯一的对象键
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectKey = "videos/" + UUID.randomUUID().toString() + extension;
            
            // 上传文件到 MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            log.info("文件上传成功: {}", objectKey);
            return objectKey;
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getVideoUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new BusinessException(400, "对象键不能为空");
        }
        
        try {
            // 生成预签名 URL，有效期 1 小时
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .expiry(1, TimeUnit.HOURS)
                    .build()
            );
            
            return url;
            
        } catch (Exception e) {
            log.error("获取视频 URL 失败: {}", objectKey, e);
            throw new BusinessException(500, "获取视频 URL 失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteVideo(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new BusinessException(400, "对象键不能为空");
        }
        
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectKey)
                    .build()
            );
            
            log.info("文件删除成功: {}", objectKey);
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectKey, e);
            throw new BusinessException(500, "文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
                );
                log.info("Bucket 创建成功: {}", minioConfig.getBucketName());
            } else {
                log.info("Bucket 已存在: {}", minioConfig.getBucketName());
            }
            
        } catch (Exception e) {
            log.error("检查或创建 Bucket 失败", e);
            throw new BusinessException(500, "MinIO 初始化失败: " + e.getMessage());
        }
    }
}
