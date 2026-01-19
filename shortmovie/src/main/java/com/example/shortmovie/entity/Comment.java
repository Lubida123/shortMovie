package com.example.shortmovie.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
    
    @Schema(description = "父评论ID，0表示一级评论")
    private Long parentId;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "评论时间")
    private LocalDateTime createTime;
    
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
