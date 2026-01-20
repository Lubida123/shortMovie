package com.example.shortmovie.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.shortmovie.entity.SensitiveWord;
import com.example.shortmovie.mapper.SensitiveWordMapper;
import com.example.shortmovie.service.SensitiveWordService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 敏感词过滤服务实现类
 * 使用DFA（确定有限状态自动机）算法实现高效的敏感词检测和过滤
 */
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {
    
    private static final Logger log = LoggerFactory.getLogger(SensitiveWordServiceImpl.class);
    
    private final SensitiveWordMapper sensitiveWordMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 敏感词DFA树的根节点
     * Map结构：key为字符，value为下一层节点
     * 特殊key "isEnd" 标记是否为敏感词结尾
     */
    private Map<String, Object> sensitiveWordMap = new HashMap<>();
    
    private static final String END_FLAG = "isEnd";
    
    // Redis缓存相关常量
    private static final String SENSITIVE_WORD_CACHE_KEY = "sensitive:words:set";
    private static final long SENSITIVE_WORD_CACHE_TTL_HOURS = 24;
    
    /**
     * 应用启动时初始化敏感词库
     */
    @PostConstruct
    public void init() {
        reloadSensitiveWords();
    }
    
    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        int length = text.length();
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public String filterSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text);
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                // 将敏感词替换为***
                for (int j = 0; j < matchLength; j++) {
                    result.setCharAt(i + j, '*');
                }
                i += matchLength - 1; // 跳过已处理的字符
            }
        }
        
        return result.toString();
    }
    
    @Override
    public void reloadSensitiveWords() {
        try {
            List<SensitiveWord> sensitiveWords;
            
            // 尝试从Redis缓存获取敏感词列表
            try {
                @SuppressWarnings("unchecked")
                Set<Object> cachedWords = redisTemplate.opsForSet().members(SENSITIVE_WORD_CACHE_KEY);
                
                if (cachedWords != null && !cachedWords.isEmpty()) {
                    // 清空现有敏感词库
                    sensitiveWordMap.clear();
                    
                    // 从缓存构建DFA树
                    for (Object word : cachedWords) {
                        addSensitiveWord(word.toString());
                    }
                    
                    log.info("从Redis缓存加载敏感词库完成，共加载 {} 个敏感词", cachedWords.size());
                    return;
                }
            } catch (Exception e) {
                log.warn("从Redis读取敏感词缓存失败，将从数据库加载: {}", e.getMessage());
            }
            
            // 缓存未命中，从数据库加载所有敏感词
            sensitiveWords = sensitiveWordMapper.selectList(null);
            
            // 清空现有敏感词库
            sensitiveWordMap.clear();
            
            // 构建DFA树
            for (SensitiveWord sensitiveWord : sensitiveWords) {
                addSensitiveWord(sensitiveWord.getWord());
            }
            
            // 将敏感词列表写入Redis缓存（使用Set类型）
            try {
                // 先删除旧缓存
                redisTemplate.delete(SENSITIVE_WORD_CACHE_KEY);
                
                // 批量添加到Set
                if (!sensitiveWords.isEmpty()) {
                    String[] words = sensitiveWords.stream()
                        .map(SensitiveWord::getWord)
                        .toArray(String[]::new);
                    redisTemplate.opsForSet().add(SENSITIVE_WORD_CACHE_KEY, (Object[]) words);
                    
                    // 设置过期时间
                    redisTemplate.expire(SENSITIVE_WORD_CACHE_KEY, SENSITIVE_WORD_CACHE_TTL_HOURS, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                log.warn("写入敏感词缓存失败: {}", e.getMessage());
            }
            
            log.info("敏感词库加载完成，共加载 {} 个敏感词", sensitiveWords.size());
        } catch (Exception e) {
            log.error("加载敏感词库失败", e);
        }
    }
    
    /**
     * 添加敏感词到DFA树
     * 
     * @param word 敏感词
     */
    private void addSensitiveWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        
        Map<String, Object> currentMap = sensitiveWordMap;
        
        for (int i = 0; i < word.length(); i++) {
            String c = String.valueOf(word.charAt(i));
            
            // 获取或创建下一层节点
            @SuppressWarnings("unchecked")
            Map<String, Object> nextMap = (Map<String, Object>) currentMap.get(c);
            
            if (nextMap == null) {
                nextMap = new HashMap<>();
                currentMap.put(c, nextMap);
            }
            
            currentMap = nextMap;
            
            // 如果是最后一个字符，标记为敏感词结尾
            if (i == word.length() - 1) {
                currentMap.put(END_FLAG, true);
            }
        }
    }
    
    /**
     * 检查从指定位置开始是否存在敏感词
     * 
     * @param text 待检查的文本
     * @param startIndex 开始位置
     * @return 匹配到的敏感词长度，如果没有匹配返回0
     */
    private int checkSensitiveWord(String text, int startIndex) {
        int matchLength = 0;
        Map<String, Object> currentMap = sensitiveWordMap;
        
        for (int i = startIndex; i < text.length(); i++) {
            String c = String.valueOf(text.charAt(i));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> nextMap = (Map<String, Object>) currentMap.get(c);
            
            if (nextMap == null) {
                // 没有匹配到，结束检查
                break;
            }
            
            matchLength++;
            
            // 检查是否为敏感词结尾
            if (nextMap.containsKey(END_FLAG)) {
                return matchLength;
            }
            
            currentMap = nextMap;
        }
        
        return 0;
    }
}
