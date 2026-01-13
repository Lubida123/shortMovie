package com.example.shortmovie.controller;

import com.example.shortmovie.dto.UserLoginDTO;
import com.example.shortmovie.dto.UserRegisterDTO;
import com.example.shortmovie.service.UserService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户接口", description = "用户注册、登录、信息管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "用户注册接口，需要提供用户名、密码、手机号、邮箱和邮箱验证码")
    @PostMapping("/register")
    public R<Void> register(
            @Parameter(description = "用户名", required = true)
            @NotBlank(message = "用户名不能为空")
            @RequestParam String username,

            @Parameter(description = "密码", required = true)
            @NotBlank(message = "密码不能为空")
            @RequestParam String password,

            @Parameter(description = "手机号", required = true)
            @NotBlank(message = "手机号不能为空")
            @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
            @RequestParam String phone,

            @Parameter(description = "邮箱", required = true)
            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            @RequestParam String email,

            @Parameter(description = "邮箱验证码", required = true)
            @NotBlank(message = "验证码不能为空")
            @RequestParam String emailCode) {

        // 构建 DTO 对象
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setEmailCode(emailCode);

        userService.register(dto);
        return R.ok();
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录接口，支持用户名/邮箱/手机号登录")
    @PostMapping("/login")
    public R<LoginVO> login(
            @Parameter(description = "登录账号（用户名/邮箱/手机号）", required = true)
            @NotBlank(message = "登录账号不能为空")
            @RequestParam String account,

            @Parameter(description = "密码", required = true)
            @NotBlank(message = "密码不能为空")
            @RequestParam String password) {

        // 构建 DTO 对象
        UserLoginDTO dto = new UserLoginDTO();
        dto.setAccount(account);
        dto.setPassword(password);

        LoginVO loginVO = userService.login(dto);
        return R.ok(loginVO);
    }

    /**
     * 发送邮箱验证码
     */
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码，验证码有效期5分钟")
    @PostMapping("/send-code")
    public R<Void> sendEmailCode(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam String email) {
        userService.sendEmailCode(email);
        return R.ok();
    }
}
