package com.example.shortmovie.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应结果")
public class R<T> {
    
    @Schema(description = "状态码：200-成功，其他-失败")
    private Integer code;
    
    @Schema(description = "响应消息")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    @Schema(description = "时间戳")
    private Long timestamp;
    
    public R() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> ok() {
        return new R<>(200, "success", null);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> R<T> error(Integer code, String message) {
        return new R<>(code, message, null);
    }
    
    /**
     * 失败响应（默认500）
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, message, null);
    }
}
