package com.example.shortmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频 Mapper 接口
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
