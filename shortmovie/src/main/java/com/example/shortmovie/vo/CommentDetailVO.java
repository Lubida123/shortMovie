package com.example.shortmovie.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论详情")
public class CommentDetailVO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "视频ID")
    private Long videoId;
    
    @Schema(description = "视频标题")
    private String videoTitle;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
