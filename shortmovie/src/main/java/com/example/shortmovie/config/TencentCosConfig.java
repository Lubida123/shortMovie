package com.example.shortmovie.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TencentCosConfig {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.region}")
    private String region;

    /**
     * 注入 COS 客户端
     */
    @Bean
    public COSClient cosClient() {
        // 1. 初始化密钥信息
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        // 2. 配置客户端地域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3. 创建 COS 客户端
        return new COSClient(credentials, clientConfig);
    }
}