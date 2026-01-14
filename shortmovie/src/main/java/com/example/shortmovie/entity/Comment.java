package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
@Schema(description = "评论实体")
public class Comment {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "视频ID")
    private Long videoId;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "评论时间")
    private LocalDateTime createTime;
    
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
