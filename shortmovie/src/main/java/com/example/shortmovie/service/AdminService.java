package com.example.shortmovie.service;

import com.example.shortmovie.dto.AdminLoginDTO;
import com.example.shortmovie.dto.AdminRegisterDTO;
import com.example.shortmovie.entity.Admin;
import com.example.shortmovie.vo.AdminLoginVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员服务
 */
public interface AdminService {

    /**
     * 管理员注册
     * @param dto
     */
    void register(AdminRegisterDTO dto);

    /**
     * 管理员登录
     * @param dto
     * @return
     */
    AdminLoginVO login(AdminLoginDTO dto);

    List<Admin> getAdminsByKeyword(String keyword);
}
