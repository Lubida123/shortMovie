package com.example.shortmovie.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.RecommendationResult;

/**
 * 推荐结果Mapper接口
 */
@Mapper
public interface RecommendationResultMapper extends BaseMapper<RecommendationResult> {
}
