package com.example.shortmovie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shortmovie.entity.UserInfo;
import com.example.shortmovie.mapper.UserInfoMapper;
import com.example.shortmovie.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo getUserByName(String username)
    {
        return userInfoMapper.selectByUsername(username);
    }
}
