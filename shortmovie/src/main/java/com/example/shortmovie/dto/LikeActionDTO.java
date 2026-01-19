package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞操作请求DTO
 */
@Data
@Schema(description = "点赞操作请求")
public class LikeActionDTO {
    
    @Schema(description = "视频ID")
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
}
