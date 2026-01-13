package com.example.shortmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.EmailVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证码Mapper接口
 */
@Mapper
public interface EmailVerificationMapper extends BaseMapper<EmailVerification> {
}
