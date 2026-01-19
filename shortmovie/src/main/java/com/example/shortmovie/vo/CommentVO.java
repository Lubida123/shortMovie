package com.example.shortmovie.vo;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论信息")
public class CommentVO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "视频ID")
    private Long videoId;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "用户头像")
    private String userAvatar;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "当前用户是否点赞")
    private Boolean isLiked;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "回复列表（二级评论）")
    private List<CommentVO> replies;
}
