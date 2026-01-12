package com.example.shortmovie.controller;

import com.example.shortmovie.entity.UserInfo;
import com.example.shortmovie.service.UserInfoService;
import com.example.shortmovie.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口")
@Slf4j
@RequestMapping("user")
@RestController
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @Operation(summary = "用户登录接口")
    @GetMapping("login")
    public R login(@RequestParam String username, @RequestParam String password)
    {
        UserInfo userInfo = userInfoService.getUserByName(username);
        if(userInfo == null)
        {
            return R.failed("您登录的用户名或密码错误！");
        }
        else
        {
            //登录成功返回用户对象
            if (userInfo.getUserpassword().equals(password)) {
                return R.success(userInfo);
            }
            else{
                return R.failed("您登录的用户名或密码错误！");
            }

        }
    }
}
