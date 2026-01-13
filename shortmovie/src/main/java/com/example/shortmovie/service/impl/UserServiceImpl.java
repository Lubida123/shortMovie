package com.example.shortmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.dto.UserLoginDTO;
import com.example.shortmovie.dto.UserRegisterDTO;
import com.example.shortmovie.entity.EmailVerification;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.exception.AuthenticationException;
import com.example.shortmovie.exception.ValidationException;
import com.example.shortmovie.mapper.EmailVerificationMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.service.UserService;
import com.example.shortmovie.utils.JwtUtil;
import com.example.shortmovie.vo.LoginVO;
import com.example.shortmovie.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO dto) {
        // 1. 验证用户名唯一性
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new ValidationException("用户名已存在");
        }
        
        // 2. 验证邮箱唯一性
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, dto.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new ValidationException("邮箱已存在");
        }
        
        // 3. 验证手机号唯一性
        LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
        phoneQuery.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectCount(phoneQuery) > 0) {
            throw new ValidationException("手机号已存在");
        }
        
        // 4. 验证邮箱验证码
        if (!validateEmailCode(dto.getEmail(), dto.getEmailCode())) {
            throw new ValidationException("验证码错误或已过期");
        }
        
        // 5. 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getUsername()); // 默认昵称为用户名
        user.setStatus(1); // 默认状态为正常
        
        userMapper.insert(user);
        
        // 6. 标记验证码为已使用
        LambdaQueryWrapper<EmailVerification> codeQuery = new LambdaQueryWrapper<>();
        codeQuery.eq(EmailVerification::getEmail, dto.getEmail())
                .eq(EmailVerification::getCode, dto.getEmailCode())
                .eq(EmailVerification::getIsUsed, 0);
        EmailVerification verification = emailVerificationMapper.selectOne(codeQuery);
        if (verification != null) {
            verification.setIsUsed(1);
            emailVerificationMapper.updateById(verification);
        }
        
        log.info("User registered successfully: {}", dto.getUsername());
    }
    
    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq(User::getUsername, dto.getAccount())
                .or()
                .eq(User::getEmail, dto.getAccount())
                .or()
                .eq(User::getPhone, dto.getAccount())
        );
        
        User user = userMapper.selectOne(query);
        
        // 2. 验证用户是否存在
        if (user == null) {
            throw new AuthenticationException("账号或密码错误");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new AuthenticationException("账号或密码错误");
        }
        
        // 4. 验证账号状态
        if (user.getStatus() == 0) {
            throw new AuthenticationException("账号已被冻结");
        }
        
        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 6. 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        
        UserProfileVO userInfo = new UserProfileVO();
        BeanUtils.copyProperties(user, userInfo);
        loginVO.setUserInfo(userInfo);
        
        log.info("User logged in successfully: {}", user.getUsername());
        
        return loginVO;
    }
    
    @Override
    public void sendEmailCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 设置过期时间为5分钟后
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        // 保存验证码到数据库
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setExpireTime(expireTime);
        verification.setIsUsed(0);
        
        emailVerificationMapper.insert(verification);
        
        // TODO: 实际项目中应该调用邮件服务发送验证码
        // 这里仅记录日志用于测试
        log.info("Email verification code sent to {}: {}", email, code);
    }
    
    @Override
    public boolean validateEmailCode(String email, String code) {
        LambdaQueryWrapper<EmailVerification> query = new LambdaQueryWrapper<>();
        query.eq(EmailVerification::getEmail, email)
                .eq(EmailVerification::getCode, code)
                .eq(EmailVerification::getIsUsed, 0)
                .gt(EmailVerification::getExpireTime, LocalDateTime.now())
                .orderByDesc(EmailVerification::getCreateTime)
                .last("LIMIT 1");
        
        EmailVerification verification = emailVerificationMapper.selectOne(query);
        return verification != null;
    }
    
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ValidationException("用户不存在");
        }
        
        UserProfileVO vo = new UserProfileVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
