package com.example.shortmovie.config;

import com.example.shortmovie.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 管理员接口完全跳过JWT验证（开发环境）
        if (requestURI.startsWith("/api/admin/")) {
            log.debug("跳过JWT验证: {}", requestURI);
            // 直接放行，不设置任何认证对象，让SecurityConfig的permitAll生效
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 从请求头中获取 token
        String token = getTokenFromRequest(request);

        // 2. 验证 token
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 3. 从 token 中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 4. 创建认证对象并设置到 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
