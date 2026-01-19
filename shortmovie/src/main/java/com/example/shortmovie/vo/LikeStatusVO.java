package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 点赞状态响应VO
 */
@Data
@Schema(description = "点赞状态响应")
public class LikeStatusVO {
    
    @Schema(description = "是否已点赞")
    private Boolean isLiked;
    
    @Schema(description = "点赞总数")
    private Long likeCount;
}
