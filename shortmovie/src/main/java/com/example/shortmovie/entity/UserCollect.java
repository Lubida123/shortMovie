package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏关系实体类
 */
@Data
@TableName("user_collect")
@Schema(description = "用户收藏关系实体")
public class UserCollect {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "视频ID")
    private Long videoId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "收藏时间")
    private LocalDateTime createTime;

    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
