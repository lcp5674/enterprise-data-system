package com.enterprise.edams.auth.security;

import com.enterprise.edams.auth.entity.User;
import com.enterprise.edams.auth.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 *
 * <p>Spring Security认证所需，从数据库加载用户信息</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情（用于Spring Security表单登录）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查账户是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(java.time.LocalDateTime.now())) {
            throw new UsernameNotFoundException("账户已锁定，请稍后重试");
        }

        // 检查账户是否禁用
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("账户已被禁用");
        }

        // 构建权限列表（这里使用默认角色，实际应从role表查询）
        List<SimpleGrantedAuthority> authorities = getAuthorities(user);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true,   // 账户未过期
                true,   // 凭证未过期
                !isAccountLocked(user),
                authorities);
    }

    /**
     * 获取用户权限列表
     */
    private List<SimpleGrantedAuthority> getAuthorities(User user) {
        // 默认角色：所有用户至少有ROLE_USER角色
        // 实际实现中应通过Feign调用permission-service获取用户完整权限
        return List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));
    }

    /**
     * 判断账户是否被锁定
     */
    private boolean isAccountLocked(User user) {
        return user.getLockTime() != null && user.getLockTime().isAfter(java.time.LocalDateTime.now());
    }
}
