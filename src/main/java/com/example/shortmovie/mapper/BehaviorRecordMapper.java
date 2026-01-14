package com.example.shortmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.BehaviorRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为记录 Mapper 接口
 */
@Mapper
public interface BehaviorRecordMapper extends BaseMapper<BehaviorRecord> {
}
