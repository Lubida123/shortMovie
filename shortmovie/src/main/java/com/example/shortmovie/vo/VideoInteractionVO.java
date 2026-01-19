package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频交互状态响应VO
 */
@Data
@Schema(description = "视频交互状态响应")
public class VideoInteractionVO {
    
    @Schema(description = "是否已点赞")
    private Boolean isLiked;
    
    @Schema(description = "是否已收藏")
    private Boolean isCollected;
}
