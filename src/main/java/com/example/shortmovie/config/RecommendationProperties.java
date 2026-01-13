package com.example.shortmovie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 推荐系统配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {
    
    /**
     * 热度分数权重配置
     */
    private Map<String, Double> heatScoreWeights;
    
    /**
     * 缓存过期时间（秒）
     */
    private Integer cacheTtl;
}
