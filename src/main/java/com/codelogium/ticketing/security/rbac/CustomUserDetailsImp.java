package com.codelogium.ticketing.security.rbac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.UserRole;

import lombok.AllArgsConstructor;

/**
 * 桥接应用层 User 实体与 Spring Security 的 UserDetails 接口。
 * Spring Security 通过本类获取用户名、密码、角色权限，完成认证与授权。
 */
@AllArgsConstructor
public class CustomUserDetailsImp implements UserDetails {

    private User user;

    /** 返回用户角色集合（Spring Security 要求返回 Collection，即使只有一个角色） */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        UserRole role = user.getRole();

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(role.toString()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    /** 账户状态相关方法，均返回 true（不由 Spring Security 管理账户过期，仅区分 JWT 过期） */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
