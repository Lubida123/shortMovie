package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员视角的视频信息VO
 */
@Data
@Schema(description = "管理员视角的视频信息")
public class AdminVideoVO {
    
    @Schema(description = "视频ID")
    private Long id;
    
    @Schema(description = "视频标题")
    private String title;
    
    @Schema(description = "封面URL")
    private String coverUrl;
    
    @Schema(description = "视频时长（秒）")
    private Integer duration;
    
    @Schema(description = "作者名称")
    private String authorName;
    
    @Schema(description = "作者头像")
    private String authorAvatar;
    
    @Schema(description = "分类")
    private String category;
    
    @Schema(description = "播放量")
    private Long views;
    
    @Schema(description = "点赞数")
    private Long likes;
    
    @Schema(description = "评论数")
    private Long comments;
    
    @Schema(description = "收藏数")
    private Long collects;
    
    @Schema(description = "状态：pending-待审核，approved-已通过，rejected-已拒绝")
    private String status;
    
    @Schema(description = "上传时间")
    private LocalDateTime createTime;
}
