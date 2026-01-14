package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Data
@TableName("video")
@Schema(description = "视频实体")
public class Video {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "视频ID")
    private Long id;
    
    @Schema(description = "视频标题")
    private String title;
    
    @Schema(description = "视频描述")
    private String description;
    
    @Schema(description = "作者ID")
    private Long authorId;
    
    @Schema(description = "作者名称")
    private String authorName;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "MinIO存储路径")
    private String videoUrl;
    
    @Schema(description = "MinIO对象键")
    private String objectKey;
    
    @Schema(description = "视频时长（秒）")
    private Integer duration;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @Schema(description = "视频格式")
    private String format;
    
    @Schema(description = "分类")
    private String category;
    
    @Schema(description = "标签（逗号分隔）")
    private String tags;
    
    @Schema(description = "审核状态：0-待审核，1-通过，2-驳回")
    private Integer auditStatus;
    
    @Schema(description = "是否热门：0-否，1-是")
    private Integer isHot;
    
    @Schema(description = "播放次数")
    private Long playCount;
    
    @Schema(description = "点赞数")
    private Long likeCount;
    
    @Schema(description = "评论数")
    private Long commentCount;
    
    @Schema(description = "收藏数")
    private Long collectCount;
    
    @Schema(description = "热度分数")
    private BigDecimal heatScore;
    
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
