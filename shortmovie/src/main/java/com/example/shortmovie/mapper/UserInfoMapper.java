package com.example.shortmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    /**
     * 根据用户名返回用户对象
     * @param username
     * @return
     */
    @Select("select * from user_info where username = #{username}")
    public UserInfo selectByUsername(String username);
}
