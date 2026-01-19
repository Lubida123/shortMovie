package com.example.shortmovie.exception;

/**
 * 重复评论异常
 */
public class DuplicateCommentException extends BusinessException {
    
    public DuplicateCommentException(String message) {
        super(429, message);
    }
}
