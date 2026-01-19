package com.example.shortmovie.service;

/**
 * 敏感词过滤服务接口
 */
public interface SensitiveWordService {
    
    /**
     * 检查文本是否包含敏感词
     * 
     * @param text 待检查的文本
     * @return 如果包含敏感词返回true，否则返回false
     */
    boolean containsSensitiveWord(String text);
    
    /**
     * 过滤敏感词（替换为***）
     * 
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    String filterSensitiveWord(String text);
    
    /**
     * 重新加载敏感词库
     * 从数据库重新加载敏感词到内存
     */
    void reloadSensitiveWords();
}
