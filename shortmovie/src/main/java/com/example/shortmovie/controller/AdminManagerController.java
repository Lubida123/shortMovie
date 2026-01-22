package com.example.shortmovie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.entity.Admin;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.mapper.AdminMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.utils.UserConverter;
import com.example.shortmovie.vo.UnifiedUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "用户管理接口", description = "管理员信息管理相关接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminManagerController {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 获取管理员列表
     */
    @Operation(summary = "获取管理员列表", description = "获取管理员列表，支持关键词搜索")
    @GetMapping("/admin/list")
    public R<Map<String, Object>> getAdminList(
            @Parameter(description = "搜索关键词（管理员名/手机号）")
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Admin::getAdminname, keyword)
                    .or()
                    .like(Admin::getPhone, keyword)
            );
        }
        wrapper.orderByDesc(Admin::getCreateTime);

        List<Admin> admins = adminMapper.selectList(wrapper);
        List<UnifiedUserVO> adminVOs = UserConverter.convertAdminsToVO(admins);

        Map<String, Object> result = new HashMap<>();
        result.put("list", adminVOs);
        result.put("total", adminVOs.size());

        return R.ok(result);
    }

    /**
     * 获取普通用户列表
     */
    @Operation(summary = "获取普通用户列表", description = "获取普通用户列表，支持关键词搜索")
    @GetMapping("/user/list")
    public R<Map<String, Object>> getUserList(
            @Parameter(description = "搜索关键词（用户名/手机号）")
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword)
            );
        }
        wrapper.orderByDesc(User::getCreateTime);

        List<User> users = userMapper.selectList(wrapper);
        List<UnifiedUserVO> userVOs = UserConverter.convertUsersToVO(users);

        Map<String, Object> result = new HashMap<>();
        result.put("list", userVOs);
        result.put("total", userVOs.size());

        return R.ok(result);
    }

    /**
     * 新增管理员
     */
    @Operation(summary = "新增管理员", description = "创建新的管理员账户")
    @PostMapping("/admin/add")
    public R<Void> addAdmin(@Validated @RequestBody Map<String, Object> params) {
        String adminname = (String) params.get("username");
        String password = (String) params.get("password");
        String phone = (String) params.get("phone");
        String status = (String) params.get("status");

        // 参数验证
        if (!StringUtils.hasText(adminname) || adminname.length() < 2 || adminname.length() > 20) {
            throw new ValidationException("管理员名长度在2-20个字符之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 20) {
            throw new ValidationException("密码长度在6-20个字符之间");
        }
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$")) {
            throw new ValidationException("密码必须包含字母和数字");
        }
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new ValidationException("手机号格式不正确");
        }

        // 验证管理员名唯一性
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getAdminname, adminname);
        if (adminMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("管理员名已存在");
        }

        // 验证手机号唯一性
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getPhone, phone);
        if (adminMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("手机号已存在");
        }

        Admin admin = new Admin();
        admin.setAdminname(adminname);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setPhone(phone);
        admin.setStatus("active".equals(status) ? 1 : 0);
        admin.setNickname(adminname);

        adminMapper.insert(admin);
        log.info("创建管理员成功: {}", adminname);
        return R.ok();
    }

    /**
     * 新增普通用户
     */
    @Operation(summary = "新增普通用户", description = "创建新的普通用户账户")
    @PostMapping("/user/add")
    public R<Void> addUser(@Validated @RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String phone = (String) params.get("phone");
        String status = (String) params.get("status");

        // 参数验证
        if (!StringUtils.hasText(username) || username.length() < 2 || username.length() > 20) {
            throw new ValidationException("用户名长度在2-20个字符之间");
        }
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 20) {
            throw new ValidationException("密码长度在6-20个字符之间");
        }
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$")) {
            throw new ValidationException("密码必须包含字母和数字");
        }
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new ValidationException("手机号格式不正确");
        }

        // 验证用户名唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("用户名已存在");
        }

        // 验证手机号唯一性
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("手机号已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setStatus("active".equals(status) ? 1 : 0);
        user.setNickname(username);

        userMapper.insert(user);
        log.info("创建普通用户成功: {}", username);
        return R.ok();
    }

    /**
     * 编辑管理员
     */
    @Operation(summary = "编辑管理员", description = "更新管理员信息")
    @PutMapping("/admin/edit")
    public R<Void> editAdmin(@Validated @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String adminname = (String) params.get("username");
        String phone = (String) params.get("phone");
        String status = (String) params.get("status");

        // 参数验证
        if (!StringUtils.hasText(adminname) || adminname.length() < 2 || adminname.length() > 20) {
            throw new ValidationException("管理员名长度在2-20个字符之间");
        }
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new ValidationException("手机号格式不正确");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new ValidationException("管理员不存在");
        }

        // 验证管理员名唯一性（排除自己）
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getAdminname, adminname).ne(Admin::getId, id);
        if (adminMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("管理员名已存在");
        }

        // 验证手机号唯一性（排除自己）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getPhone, phone).ne(Admin::getId, id);
        if (adminMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("手机号已存在");
        }

        admin.setAdminname(adminname);
        admin.setPhone(phone);
        admin.setStatus("active".equals(status) ? 1 : 0);

        adminMapper.updateById(admin);
        log.info("更新管理员信息成功: id={}, adminname={}", id, adminname);
        return R.ok();
    }

    /**
     * 编辑普通用户
     */
    @Operation(summary = "编辑普通用户", description = "更新普通用户信息")
    @PutMapping("/user/edit")
    public R<Void> editUser(@Validated @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String username = (String) params.get("username");
        String phone = (String) params.get("phone");
        String status = (String) params.get("status");

        // 参数验证
        if (!StringUtils.hasText(username) || username.length() < 2 || username.length() > 20) {
            throw new ValidationException("用户名长度在2-20个字符之间");
        }
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new ValidationException("手机号格式不正确");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ValidationException("用户不存在");
        }

        // 验证用户名唯一性（排除自己）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).ne(User::getId, id);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("用户名已存在");
        }

        // 验证手机号唯一性（排除自己）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone).ne(User::getId, id);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ValidationException("手机号已存在");
        }

        user.setUsername(username);
        user.setPhone(phone);
        user.setStatus("active".equals(status) ? 1 : 0);

        userMapper.updateById(user);
        log.info("更新普通用户信息成功: id={}, username={}", id, username);
        return R.ok();
    }

    /**
     * 修改管理员状态
     */
    @Operation(summary = "修改管理员状态", description = "启用或禁用管理员账户")
    @PutMapping("/admin/changeStatus")
    public R<Void> changeAdminStatus(@Validated @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = (String) params.get("status");

        if (!StringUtils.hasText(status) || (!status.equals("active") && !status.equals("disabled"))) {
            throw new ValidationException("状态参数不正确");
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new ValidationException("管理员不存在");
        }

        admin.setStatus("active".equals(status) ? 1 : 0);
        adminMapper.updateById(admin);

        log.info("修改管理员状态成功: id={}, status={}", id, status);
        return R.ok();
    }

    /**
     * 修改普通用户状态
     */
    @Operation(summary = "修改普通用户状态", description = "启用或禁用普通用户账户")
    @PutMapping("/user/changeStatus")
    public R<Void> changeUserStatus(@Validated @RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String status = (String) params.get("status");

        if (!StringUtils.hasText(status) || (!status.equals("active") && !status.equals("disabled"))) {
            throw new ValidationException("状态参数不正确");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ValidationException("用户不存在");
        }

        user.setStatus("active".equals(status) ? 1 : 0);
        userMapper.updateById(user);

        log.info("修改普通用户状态成功: id={}, status={}", id, status);
        return R.ok();
    }

    /**
     * 删除管理员
     */
    @Operation(summary = "删除管理员", description = "删除管理员账户")
    @DeleteMapping("/admin/delete/{id}")
    public R<Void> deleteAdmin(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new ValidationException("管理员不存在");
        }

        adminMapper.deleteById(id);
        log.info("删除管理员成功: id={}, adminname={}", id, admin.getAdminname());
        return R.ok();
    }

    /**
     * 删除普通用户
     */
    @Operation(summary = "删除普通用户", description = "删除普通用户账户")
    @DeleteMapping("/user/delete/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ValidationException("用户不存在");
        }

        userMapper.deleteById(id);
        log.info("删除普通用户成功: id={}, username={}", id, user.getUsername());
        return R.ok();
    }
}
