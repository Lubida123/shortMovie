package com.example.shortmovie.dto;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论创建请求DTO
 */
@Data
@Schema(description = "评论创建请求")
public class CommentCreateDTO {
    
    @Schema(description = "视频ID")
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    
    @Schema(description = "评论内容")
    @NotBlank(message = "评论内容不能为空")
    @Length(max = 500, message = "评论内容不能超过500字符")
    private String content;
    
    @Schema(description = "父评论ID，为null表示一级评论")
    private Long parentId;
}
