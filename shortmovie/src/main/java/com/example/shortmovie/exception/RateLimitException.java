package com.example.shortmovie.exception;

/**
 * 限流异常
 * 当用户请求超过限流阈值时抛出
 */
public class RateLimitException extends RuntimeException {
    
    private final int code;
    
    public RateLimitException(String message) {
        super(message);
        this.code = 429;
    }
    
    public RateLimitException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}
