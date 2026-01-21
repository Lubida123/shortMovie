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
 * 模型参数实体类
 */
@Data
@TableName("model_params")
@Schema(description = "模型参数实体")
public class ModelParams {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模型ID")
    private String modelId;

    @TableField("`rank`")
    @Schema(description = "ALS隐因子数量")
    private Integer rank;

    @Schema(description = "正则化参数")
    private Double regParam;

    @Schema(description = "最大迭代次数")
    private Integer maxIter;

    @Schema(description = "训练时间")
    private LocalDateTime trainingTime;

    @Schema(description = "模型存储路径")
    private String modelPath;

    @Schema(description = "模型RMSE评估指标")
    private Double rmse;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
