package com.example.shortmovie.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    /**
     * 密码编码器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 允许 Knife4j 文档访问
                        .requestMatchers(
                                "/doc.html",
                                "/doc.html/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        // 允许用户注册和登录接口
                        .requestMatchers(
                                "/api/user/register",
                                "/api/user/login",
                                "/api/user/send-code",
                                "/api/video/upload",  // 临时允许视频上传（仅用于测试）
                                "/api/video/list",    // 允许匿名访问视频列表
                                "/api/video/*/detail", // 允许匿名访问视频详情
                                "/api/admin/login",
                                "/api/admin/register"
                        ).permitAll()
                        .requestMatchers(
                                "/api/admin/roles",           // 角色列表
                                "/api/admin/roles/**",        // 角色相关操作
                                "/api/admin/permissions/**"   // 权限相关操作
                        ).permitAll()
                        .requestMatchers(
                                "/api/admin/admin/list",      // 获取管理员列表
                                "/api/admin/user/list",       // 获取普通用户列表
                                "/api/admin/admin/add",       // 新增管理员
                                "/api/admin/user/add",        // 新增普通用户
                                "/api/admin/admin/edit",      // 编辑管理员
                                "/api/admin/user/edit",       // 编辑普通用户
                                "/api/admin/admin/changeStatus", // 修改管理员状态
                                "/api/admin/user/changeStatus",  // 修改普通用户状态
                                "/api/admin/admin/delete/**", // 删除管理员
                                "/api/admin/user/delete/**"   // 删除普通用户
                        ).permitAll()
                        .requestMatchers(
                                "/api/admin/video/list",
                                "/api/admin/video/stats",
                                "/api/admin/video/approve/**",
                                "/api/admin/video/reject/**",
                                "/api/admin/video/delete/**",
                                "/api/admin/video/batch/**"
                        ).permitAll()
                        // 允许数据分析接口（测试用）
                        .requestMatchers(
                                "/api/admin/analysis/**"      // 数据分析接口
                        ).permitAll()
                        .requestMatchers("/sendmsg").permitAll()
                        .requestMatchers("/analysis/movie").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加 JWT 认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
