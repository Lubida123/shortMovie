package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频上传响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "视频上传响应")
public class VideoUploadVO {
    
    @Schema(description = "视频ID")
    private Long videoId;
    
    @Schema(description = "MinIO 对象键")
    private String objectKey;
    
    @Schema(description = "视频访问 URL")
    private String videoUrl;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
}
