package com.example.shortmovie.service.impl;

import com.example.shortmovie.exception.BusinessException;
import com.example.shortmovie.service.FileStorageService;
import com.example.shortmovie.utils.TencentCosUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 腾讯云 COS 文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tencent.cos.enabled", havingValue = "true", matchIfMissing = false)
public class TencentCosStorageServiceImpl implements FileStorageService {
    
    private final TencentCosUtil cosUtil;
    
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            return cosUtil.uploadFile(file);
        } catch (Exception e) {
            log.error("COS 文件上传失败", e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getFileUrl(String objectKey) {
        try {
            return cosUtil.getFileAccessUrl(objectKey);
        } catch (Exception e) {
            log.error("获取 COS 文件 URL 失败: {}", objectKey, e);
            throw new BusinessException(500, "获取文件 URL 失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String objectKey) {
        try {
            cosUtil.deleteFile(objectKey);
        } catch (Exception e) {
            log.error("COS 文件删除失败: {}", objectKey, e);
            throw new BusinessException(500, "文件删除失败: " + e.getMessage());
        }
    }
}
