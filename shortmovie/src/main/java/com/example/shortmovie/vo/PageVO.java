package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageVO<T> {
    
    @Schema(description = "当前页码")
    private Integer pageNum;
    
    @Schema(description = "每页大小")
    private Integer pageSize;
    
    @Schema(description = "总记录数")
    private Long total;
    
    @Schema(description = "总页数")
    private Integer pages;
    
    @Schema(description = "数据列表")
    private List<T> records;
}
