package com.enterprise.edams.auth.filter;

import com.enterprise.edams.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token) && tokenService.validateToken(token)) {
                // 检查Token是否在黑名单
                if (!tokenService.isTokenBlacklisted(token)) {
                    Map<String, Object> claims = tokenService.parseAccessToken(token);
                    
                    if (claims != null) {
                        String userId = (String) claims.get("userId");
                        String username = (String) claims.get("username");
                        
                        // 获取权限列表
                        @SuppressWarnings("unchecked")
                        List<String> permissions = (List<String>) claims.get("permissions");
                        if (permissions == null) {
                            permissions = Collections.emptyList();
                        }

                        List<SimpleGrantedAuthority> authorities = permissions.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        authentication.setDetails(username);

                        // 设置到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("JWT认证成功: userId={}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证失败", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") 
            || path.startsWith("/api/v1/auth/logout")
            || path.startsWith("/api/v1/auth/refresh")
            || path.startsWith("/api/v1/auth/captcha")
            || path.startsWith("/api/v1/auth/mfa/verify")
            || path.startsWith("/actuator")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs");
    }
}
