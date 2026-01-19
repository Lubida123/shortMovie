package com.example.shortmovie.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.shortmovie.entity.SensitiveWord;
import com.example.shortmovie.mapper.SensitiveWordMapper;
import com.example.shortmovie.service.SensitiveWordService;
import com.example.shortmovie.utils.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 敏感词管理控制器（可选功能）
 * 用于管理员管理敏感词库
 */
@Tag(name = "敏感词管理", description = "敏感词管理接口（管理员功能）")
@RestController
@RequestMapping("/api/admin/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "获取所有敏感词")
    @GetMapping
    public R<List<SensitiveWord>> getAllSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        return R.ok(words);
    }

    @Operation(summary = "添加敏感词")
    @PostMapping
    public R<Void> addSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        if (sensitiveWord.getLevel() == null) {
            sensitiveWord.setLevel(1);
        }
        sensitiveWordMapper.insert(sensitiveWord);
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        return R.ok();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    public R<Void> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordMapper.deleteById(id);
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        return R.ok();
    }

    @Operation(summary = "批量添加敏感词")
    @PostMapping("/batch")
    public R<Void> batchAddSensitiveWords(@RequestBody List<String> words) {
        for (String word : words) {
            try {
                SensitiveWord sensitiveWord = new SensitiveWord();
                sensitiveWord.setWord(word);
                sensitiveWord.setLevel(1);
                sensitiveWordMapper.insert(sensitiveWord);
            } catch (Exception e) {
                // 忽略重复词
            }
        }
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        return R.ok();
    }

    @Operation(summary = "重新加载敏感词库")
    @PostMapping("/reload")
    public R<Void> reloadSensitiveWords() {
        sensitiveWordService.reloadSensitiveWords();
        return R.ok();
    }

    @Operation(summary = "测试敏感词检测")
    @PostMapping("/test")
    public R<Boolean> testSensitiveWord(@RequestBody String text) {
        boolean contains = sensitiveWordService.containsSensitiveWord(text);
        return R.ok(contains);
    }
}
