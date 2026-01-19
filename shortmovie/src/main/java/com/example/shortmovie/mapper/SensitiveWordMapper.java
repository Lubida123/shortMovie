package com.example.shortmovie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.SensitiveWord;

/**
 * 敏感词 Mapper 接口
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
