package com.example.shortmovie.controller;

import com.example.shortmovie.dto.ChangePasswordDTO;
import com.example.shortmovie.dto.UserLoginDTO;
import com.example.shortmovie.dto.UserRegisterDTO;
import com.example.shortmovie.dto.UserUpdateDTO;
import com.example.shortmovie.service.CollectService;
import com.example.shortmovie.service.LikeService;
import com.example.shortmovie.service.UserService;
import com.example.shortmovie.service.UserVideoService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.LoginVO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.UserProfileVO;
import com.example.shortmovie.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@Tag(name = "用户接口", description = "用户注册、登录、信息管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    private final UserVideoService userVideoService;
    private final LikeService likeService;
    private final CollectService collectService;
    
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
     * 用户退出登录
     */
    @Operation(summary = "退出登录", description = "用户退出登录接口（前端需清除本地存储的 token）")
    @PostMapping("/logout")
    public R<Void> logout() {
        // 由于使用的是无状态的 JWT，服务端不需要做任何处理
        // 前端只需要删除本地存储的 token 即可
        // 如果需要实现 token 黑名单功能，可以在这里将 token 加入 Redis 黑名单
        
        Long userId = getCurrentUserId();
        log.info("User logged out: userId={}", userId);
        
        return R.ok();
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
    
    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息（不包含密码等敏感字段）")
    @GetMapping("/profile")
    public R<UserProfileVO> getProfile() {
        Long userId = getCurrentUserId();
        UserProfileVO profile = userService.getUserProfile(userId);
        return R.ok(profile);
    }
    
    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息，支持更新手机号、邮箱、昵称、头像")
    @PutMapping("/profile")
    public R<Void> updateProfile(
            @Parameter(description = "手机号")
            @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
            @RequestParam(required = false) String phone,
            
            @Parameter(description = "邮箱")
            @Email(message = "邮箱格式不正确")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "昵称")
            @RequestParam(required = false) String nickname,
            
            @Parameter(description = "头像URL")
            @RequestParam(required = false) String avatar) {
        
        // 构建 DTO 对象
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setNickname(nickname);
        dto.setAvatar(avatar);
        
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, dto);
        return R.ok();
    }
    
    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需要提供旧密码和新密码")
    @PutMapping("/password")
    public R<Void> changePassword(
            @Parameter(description = "旧密码", required = true)
            @NotBlank(message = "旧密码不能为空")
            @RequestParam String oldPassword,
            
            @Parameter(description = "新密码", required = true)
            @NotBlank(message = "新密码不能为空")
            @RequestParam String newPassword) {
        
        // 构建 DTO 对象
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        
        Long userId = getCurrentUserId();
        userService.changePassword(userId, dto);
        
        return R.ok();
    }
    
    /**
     * 查询我的视频
     */
    @Operation(summary = "查询我的视频", description = "查询当前登录用户发布的所有视频，支持按审核状态筛选和分页")
    @GetMapping("/videos")
    public R<PageVO<VideoVO>> getUserVideos(
            @Parameter(description = "审核状态：0-待审核，1-通过，2-驳回")
            @RequestParam(required = false) Integer auditStatus,
            
            @Parameter(description = "页码，默认为1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小，默认为10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = getCurrentUserId();
        PageVO<VideoVO> result;
        
        if (auditStatus != null) {
            result = userVideoService.getUserVideoListByStatus(userId, auditStatus, pageNum, pageSize);
        } else {
            result = userVideoService.getUserVideoList(userId, pageNum, pageSize);
        }
        
        return R.ok(result);
    }
    
    /**
     * 查询我的喜欢列表
     */
    @Operation(summary = "查询我的喜欢列表", description = "查询当前登录用户点赞的所有视频，按点赞时间倒序排列")
    @GetMapping("/likes")
    public R<PageVO<VideoVO>> getUserLikes(
            @Parameter(description = "页码，默认为1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小，默认为10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = getCurrentUserId();
        PageVO<VideoVO> result = likeService.getUserLikeList(userId, pageNum, pageSize);
        
        return R.ok(result);
    }
    
    /**
     * 查询我的收藏列表
     */
    @Operation(summary = "查询我的收藏列表", description = "查询当前登录用户收藏的所有视频，按收藏时间倒序排列")
    @GetMapping("/collects")
    public R<PageVO<VideoVO>> getUserCollects(
            @Parameter(description = "页码，默认为1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小，默认为10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = getCurrentUserId();
        PageVO<VideoVO> result = collectService.getUserCollectList(userId, pageNum, pageSize);
        
        return R.ok(result);
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
