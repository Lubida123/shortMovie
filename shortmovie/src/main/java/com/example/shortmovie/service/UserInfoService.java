package com.example.shortmovie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shortmovie.entity.UserInfo;

public interface UserInfoService extends IService<UserInfo> {
    UserInfo getUserByName(String username);
}
