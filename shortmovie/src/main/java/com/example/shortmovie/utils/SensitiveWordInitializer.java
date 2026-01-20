package com.example.shortmovie.utils;

import com.example.shortmovie.entity.SensitiveWord;
import com.example.shortmovie.mapper.SensitiveWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 敏感词初始化器
 * 在应用启动时检查并初始化敏感词库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordInitializer implements CommandLineRunner {

    private final SensitiveWordMapper sensitiveWordMapper;

    @Override
    public void run(String... args) {
        log.info("检查敏感词库初始化状态...");
        
        Long count = sensitiveWordMapper.selectCount(null);
        if (count == 0) {
            log.warn("敏感词库为空，建议通过数据库迁移脚本初始化敏感词");
        } else {
            log.info("敏感词库已初始化，共有 {} 个敏感词", count);
        }
    }

    /**
     * 批量添加敏感词（用于程序化初始化）
     * 
     * @param words 敏感词列表
     * @param level 敏感级别
     */
    public void batchAddSensitiveWords(List<String> words, Integer level) {
        for (String word : words) {
            try {
                SensitiveWord sensitiveWord = new SensitiveWord();
                sensitiveWord.setWord(word);
                sensitiveWord.setLevel(level);
                sensitiveWordMapper.insert(sensitiveWord);
            } catch (Exception e) {
                log.warn("添加敏感词失败: {}, 原因: {}", word, e.getMessage());
            }
        }
        log.info("批量添加敏感词完成，共添加 {} 个词", words.size());
    }

    /**
     * 获取默认敏感词列表（用于测试或备用初始化）
     */
    public static List<String> getDefaultSensitiveWords() {
        return Arrays.asList(
            // 低级别
            "垃圾", "傻瓜", "白痴", "笨蛋", "废物", "无聊", "恶心", "讨厌",
            // 中级别
            "骗子", "诈骗", "欺诈", "色情", "赌博", "毒品", "暴力", "血腥", "恐怖", "仇恨",
            // 高级别
            "反动", "政治", "敏感", "违法", "犯罪", "恐怖主义", "极端", "分裂"
        );
    }
}
