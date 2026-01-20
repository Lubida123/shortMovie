package com.example.shortmovie.exception;

/**
 * 敏感词异常
 */
public class SensitiveWordException extends ValidationException {
    
    public SensitiveWordException(String message) {
        super(message);
    }
}
