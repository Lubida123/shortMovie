package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频信息")
public class VideoVO {
    
    @Schema(description = "视频ID")
    private Long id;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "作者名称")
    private String authorName;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "视频URL")
    private String videoUrl;
    
    @Schema(description = "时长（秒）")
    private Integer duration;
    
    @Schema(description = "播放次数")
    private Long playCount;
    
    @Schema(description = "点赞数")
    private Long likeCount;
    
    @Schema(description = "评论数")
    private Long commentCount;
    
    @Schema(description = "收藏数")
    private Long collectCount;
    
    @Schema(description = "是否已点赞")
    private Boolean isLiked;
    
    @Schema(description = "是否已收藏")
    private Boolean isCollected;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
