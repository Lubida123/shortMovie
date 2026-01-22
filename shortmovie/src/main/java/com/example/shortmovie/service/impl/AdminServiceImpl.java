package com.example.shortmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.dto.AdminLoginDTO;
import com.example.shortmovie.dto.AdminRegisterDTO;
import com.example.shortmovie.entity.Admin;
import com.example.shortmovie.entity.EmailVerification;
import com.example.shortmovie.entity.Permission;
import com.example.shortmovie.entity.Role;
import com.example.shortmovie.exception.AuthenticationException;
import com.example.shortmovie.exception.ValidationException;
import com.example.shortmovie.mapper.AdminMapper;
import com.example.shortmovie.mapper.EmailVerificationMapper;
import com.example.shortmovie.mapper.PermissionMapper;
import com.example.shortmovie.mapper.UserRoleMapper;
import com.example.shortmovie.service.AdminService;
import com.example.shortmovie.service.UserService;
import com.example.shortmovie.utils.JwtUtil;
import com.example.shortmovie.vo.AdminLoginVO;
import com.example.shortmovie.vo.AdminProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(AdminRegisterDTO dto){
        // 1. 验证用户名唯一性
        LambdaQueryWrapper<Admin> adminnameQuery = new LambdaQueryWrapper<>();
        adminnameQuery.eq(Admin::getAdminname, dto.getAdminname());
        if (adminMapper.selectCount(adminnameQuery) > 0) {
            throw new ValidationException("用户名已存在");
        }

        // 2. 验证邮箱唯一性
        LambdaQueryWrapper<Admin> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(Admin::getEmail, dto.getEmail());
        if (adminMapper.selectCount(emailQuery) > 0) {
            throw new ValidationException("邮箱已存在");
        }

        // 3. 验证手机号唯一性
        LambdaQueryWrapper<Admin> phoneQuery = new LambdaQueryWrapper<>();
        phoneQuery.eq(Admin::getPhone, dto.getPhone());
        if (adminMapper.selectCount(phoneQuery) > 0) {
            throw new ValidationException("手机号已存在");
        }

        // 4. 验证邮箱验证码
        if (!userService.validateEmailCode(dto.getEmail(), dto.getEmailCode())) {
            throw new ValidationException("验证码错误或已过期");
        }

        // 5. 创建用户
        Admin admin = new Admin();
        admin.setAdminname(dto.getAdminname());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        admin.setNickname(dto.getAdminname()); // 默认昵称为用户名
        admin.setStatus(1); // 默认状态为正常

        adminMapper.insert(admin);

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

        log.info("User registered successfully: {}", dto.getAdminname());
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO dto){
        LambdaQueryWrapper<Admin> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq(Admin::getAdminname, dto.getAccount())
                .or()
                .eq(Admin::getEmail, dto.getAccount())
                .or()
                .eq(Admin::getPhone, dto.getAccount())
        );

        Admin admin = adminMapper.selectOne(query);

        if(admin == null){
            throw new AuthenticationException("账号不存在");
        }

        if(!passwordEncoder.matches(dto.getPassword(), admin.getPassword())){
            throw new AuthenticationException("账号或密码错误");
        }

        if (admin.getStatus() == 0) {
            throw new AuthenticationException("账号已被冻结");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getAdminname());

        AdminLoginVO adminLoginVO = new AdminLoginVO();
        adminLoginVO.setToken(token);

        AdminProfileVO adminInfo = new AdminProfileVO();
        BeanUtils.copyProperties(admin, adminInfo);

        // 从数据库查询管理员的角色和权限
        List<String> roles = getAdminRoles(admin.getId());
        List<String> permissions = getAdminPermissions(admin.getId());

        // 设置角色（取第一个角色，或默认为admin）
        adminInfo.setRole(roles.isEmpty() ? "admin" : roles.get(0));
        adminInfo.setPermissions(permissions);

        adminLoginVO.setUserInfo(adminInfo);

        log.info("Admin logged in successfully: {}, roles: {}, permissions: {}",
                admin.getAdminname(), roles, permissions);

        return adminLoginVO;
    }

    /**
     * 获取管理员的角色列表
     */
    private List<String> getAdminRoles(Long adminId) {
        List<Role> roles = userRoleMapper.selectRolesByUserId(adminId);
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取管理员的权限列表
     */
    private List<String> getAdminPermissions(Long adminId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(adminId);
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 安全地构建管理员信息
     * @param admin
     * @return
     */
    private AdminProfileVO buildSafeAdminProfile(Admin admin){
        AdminProfileVO adminInfo = new AdminProfileVO();

        adminInfo.setId(admin.getId());
        adminInfo.setAdminname(admin.getAdminname());
        adminInfo.setPhone(admin.getPhone());
        adminInfo.setEmail(admin.getEmail());
        adminInfo.setNickname(admin.getNickname());
        adminInfo.setStatus(admin.getStatus());
        adminInfo.setCreateTime(admin.getCreateTime());

        return adminInfo;
    }

    /**
     * 根据关键词查询管理员列表
     */
    @Override
    public List<Admin> getAdminsByKeyword(String keyword) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Admin::getAdminname, keyword)
                    .or()
                    .like(Admin::getPhone, keyword)
                    .or()
                    .like(Admin::getEmail, keyword)
            );
        }
        wrapper.orderByDesc(Admin::getCreateTime);
        return adminMapper.selectList(wrapper);
    }

    /**
     * 验证管理员名唯一性
     */
    public boolean isAdminnameExists(String adminname, Long excludeId) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getAdminname, adminname);
        if (excludeId != null) {
            wrapper.ne(Admin::getId, excludeId);
        }
        return adminMapper.selectCount(wrapper) > 0;
    }

    /**
     * 验证手机号唯一性
     */
    public boolean isPhoneExists(String phone, Long excludeId) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getPhone, phone);
        if (excludeId != null) {
            wrapper.ne(Admin::getId, excludeId);
        }
        return adminMapper.selectCount(wrapper) > 0;
    }
}
