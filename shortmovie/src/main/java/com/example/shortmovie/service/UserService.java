package com.example.shortmovie.service;

import com.example.shortmovie.dto.UserLoginDTO;
import com.example.shortmovie.dto.UserRegisterDTO;
import com.example.shortmovie.vo.LoginVO;
import com.example.shortmovie.vo.UserProfileVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param dto 注册信息
     */
    void register(UserRegisterDTO dto);
    
    /**
     * 用户登录
     * @param dto 登录信息
     * @return 登录响应（包含token和用户信息）
     */
    LoginVO login(UserLoginDTO dto);
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     */
    void sendEmailCode(String email);
    
    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean validateEmailCode(String email, String code);
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserProfileVO getUserProfile(Long userId);
}
