package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 播放记录请求 DTO
 */
@Data
@Schema(description = "播放记录请求")
public class PlayRecordDTO {
    
    @Schema(description = "播放时长（秒）")
    private Integer playDuration;
    
    @Schema(description = "是否完播")
    private Boolean isCompleted;
}
