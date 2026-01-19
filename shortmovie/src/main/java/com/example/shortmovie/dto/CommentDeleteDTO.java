package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论删除请求DTO
 */
@Data
@Schema(description = "评论删除请求")
public class CommentDeleteDTO {
    
    @Schema(description = "评论ID")
    @NotNull(message = "评论ID不能为空")
    private Long commentId;
}
