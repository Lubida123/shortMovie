package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频上传请求 DTO
 */
@Data
@Schema(description = "视频上传请求")
public class VideoUploadDTO {
    
    @Schema(description = "视频标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "视频标题不能为空")
    private String title;
    
    @Schema(description = "视频描述")
    private String description;
    
    @Schema(description = "视频分类")
    private String category;
    
    @Schema(description = "视频标签（逗号分隔）")
    private String tags;
    
    @Schema(description = "视频时长（秒）")
    private Integer duration;
}
