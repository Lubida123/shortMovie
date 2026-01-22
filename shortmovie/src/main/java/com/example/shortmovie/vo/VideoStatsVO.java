package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频统计数据VO
 */
@Data
@Schema(description = "视频统计数据")
public class VideoStatsVO {
    
    @Schema(description = "总视频数")
    private Long totalVideos;
    
    @Schema(description = "待审核视频数")
    private Long pendingVideos;
    
    @Schema(description = "已通过视频数")
    private Long approvedVideos;
    
    @Schema(description = "已拒绝视频数")
    private Long rejectedVideos;
}
