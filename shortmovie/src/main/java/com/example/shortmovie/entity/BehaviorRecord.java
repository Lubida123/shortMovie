package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

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
    private LocalDateTime createTime;
}
