package com.example.shortmovie.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户行为记录实体类
 */
@Data
@TableName("behavior_record")
@Schema(description = "用户行为记录实体")
public class BehaviorRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "视频ID")
    private Long videoId;

    @Schema(description = "行为类型：PLAY/LIKE/COMMENT/COLLECT")
    private String behaviorType;

    @Schema(description = "播放时长（秒）")
    private Integer playDuration;

    @Schema(description = "是否完播：0-否，1-是")
    private Integer isCompleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "行为时间")
    @JsonIgnore  // 发送到Kafka时忽略此字段，使用timestamp代替
    private LocalDateTime createTime;

    /**
     * 时间戳字段（毫秒），用于Kafka消息传输
     * 不映射到数据库表，仅用于与实时推荐系统通信
     */
    @TableField(exist = false)
    @Schema(description = "行为时间戳（毫秒）")
    private Long timestamp;

    /**
     * 准备发送到Kafka前的数据转换
     * 将createTime转换为timestamp（毫秒级时间戳）
     */
    public void prepareForKafka() {
        if (this.createTime != null) {
            this.timestamp = this.createTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } else {
            // 如果createTime为空，使用当前时间
            this.timestamp = System.currentTimeMillis();
        }
    }
}
