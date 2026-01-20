package com.example.shortmovie.controller;

import com.example.shortmovie.dto.AdminLoginDTO;
import com.example.shortmovie.dto.AdminRegisterDTO;
import com.example.shortmovie.service.AdminService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "管理接口", description = "管理员登录、信息管理相关接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "管理员注册", description = "管理员注册接口，需要提供管理员名、密码、手机号、邮箱和邮箱验证码")
    @PostMapping("/register")
    public R<Void> register(
            @Parameter(description = "管理员名", required = true)
            @NotBlank(message = "管理员名不能为空")
            @RequestParam String adminname,

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
            @RequestParam String emailCode){

        AdminRegisterDTO dto = new AdminRegisterDTO();
        dto.setAdminname(adminname);
        dto.setPassword(password);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setEmailCode(emailCode);

        adminService.register(dto);
        return R.ok();
    }

    /**
     * 管理员登录
     */
    @Operation(summary = "管理员登录", description = "管理员登录接口，支持用户名/邮箱/手机号登录")
    @PostMapping("/login")
    public R<AdminLoginVO> login(
            @Parameter(description = "登录账号（用户名/邮箱/手机号）", required = true)
            @NotBlank(message = "登录账号不能为空")
            @RequestParam String account,

            @Parameter(description = "密码", required = true)
            @NotBlank(message = "密码不能为空")
            @RequestParam String password){
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAccount(account);
        dto.setPassword(password);

        AdminLoginVO loginVO = adminService.login(dto);
        return R.ok(loginVO);
    }

}
