package com.example.shortmovie.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.BehaviorRecord;

/**
 * 用户行为记录 Mapper 接口
 */
@Mapper
public interface BehaviorRecordMapper extends BaseMapper<BehaviorRecord> {
    
    /**
     * 插入或更新行为记录（使用 ON DUPLICATE KEY UPDATE）
     * 如果记录已存在（基于 user_id, video_id, behavior_type 唯一约束），则更新
     * 否则插入新记录
     */
    @Insert("INSERT INTO behavior_record (user_id, video_id, behavior_type, play_duration, is_completed, create_time) " +
            "VALUES (#{userId}, #{videoId}, #{behaviorType}, #{playDuration}, #{isCompleted}, #{createTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "play_duration = #{playDuration}, " +
            "is_completed = #{isCompleted}, " +
            "create_time = #{createTime}")
    int saveOrUpdate(BehaviorRecord behaviorRecord);
}
