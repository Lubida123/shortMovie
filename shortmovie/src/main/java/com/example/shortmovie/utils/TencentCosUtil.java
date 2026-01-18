package com.example.shortmovie.utils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Slf4j
@Component
public class TencentCosUtil {

    @Resource
    private COSClient cosClient;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.base-path}")
    private String basePath;

    @Value("${tencent.cos.expire-time}")
    private int expireTime;

    /**
     * 上传文件（MultipartFile 类型，适配 Spring Boot 文件上传）
     * @param file 上传的文件
     * @return 文件在 COS 中的对象键（object key）
     */
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("开始上传文件到腾讯云 COS...");
        
        // 1. 获取文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名称不能为空");
        }
        log.info("文件名: {}, 文件大小: {} bytes", originalFilename, file.getSize());
        
        // 2. 拼接 COS 存储路径（基础路径 + 文件名，避免重复可加时间戳）
        String cosFilePath = basePath + System.currentTimeMillis() + "_" + originalFilename;
        log.info("COS 存储路径: {}", cosFilePath);
        
        // 3. 将 MultipartFile 转为 File
        log.info("正在创建临时文件...");
        File tempFile = File.createTempFile("cos_temp", null);
        file.transferTo(tempFile);
        log.info("临时文件创建成功: {}", tempFile.getAbsolutePath());
        
        // 4. 上传文件到 COS
        log.info("正在上传到腾讯云 COS，存储桶: {}", bucketName);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, cosFilePath, tempFile);
        // 注意：不设置存储类型，由存储桶默认配置决定（兼容多可用区存储桶）
        PutObjectResult result = cosClient.putObject(putObjectRequest);
        log.info("文件上传成功！ETag: {}", result.getETag());
        
        // 5. 删除临时文件
        boolean deleted = tempFile.delete();
        log.info("临时文件删除{}", deleted ? "成功" : "失败");
        
        // 6. 返回对象键（不是完整 URL，URL 由 getFileAccessUrl 方法生成）
        log.info("返回对象键: {}", cosFilePath);
        return cosFilePath;
    }

    /**
     * 下载文件到本地
     * @param cosFilePath COS 中的文件路径
     * @param localPath 本地保存路径
     */
    public void downloadFile(String cosFilePath, String localPath) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, cosFilePath);
        File localFile = new File(localPath);
        // 确保父目录存在
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdirs();
        }
        cosClient.getObject(getObjectRequest, localFile);
    }

    /**
     * 删除 COS 中的文件
     * @param cosFilePath COS 中的文件路径
     */
    public void deleteFile(String cosFilePath) {
        cosClient.deleteObject(bucketName, cosFilePath);
    }

    /**
     * 获取文件的临时访问链接（带签名，过期后失效）
     * @param cosFilePath COS 中的文件路径
     * @return 临时访问 URL
     */
    public String getFileAccessUrl(String cosFilePath) {
        Date expiration = new Date(System.currentTimeMillis() + expireTime * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, cosFilePath);
        request.setExpiration(expiration);
        URL url = cosClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 关闭 COS 客户端（项目关闭时调用，释放资源）
     */
    public void closeClient() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }
}
