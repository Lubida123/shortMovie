package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏操作请求DTO
 */
@Data
@Schema(description = "收藏操作请求")
public class CollectActionDTO {
    
    @Schema(description = "视频ID")
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
}
