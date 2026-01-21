package com.example.shortmovie.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 推荐结果实体类
 */
@Data
@TableName("recommendation_result")
@Schema(description = "推荐结果实体")
public class RecommendationResult {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "视频ID")
    private Long movieId;

    @Schema(description = "推荐分数")
    private Double score;

    @TableField("`rank`")
    @Schema(description = "排名")
    private Integer rank;

    @TableField("`type`")
    @Schema(description = "推荐类型：OFFLINE/REAL_TIME")
    private String type;

    @Schema(description = "模型ID")
    private String modelId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
