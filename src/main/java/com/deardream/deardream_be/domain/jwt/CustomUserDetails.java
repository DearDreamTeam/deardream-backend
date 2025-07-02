package com.deardream.deardream_be.domain.jwt;

import com.deardream.deardream_be.domain.user.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final Long kakaoId;
    private final Role role;

    public CustomUserDetails(Long userId, Long kakaoId, Role role) {
        this.userId = userId;
        this.kakaoId = kakaoId;
        this.role = role;
    }

    // admin 권한이 있는지 확인하기 위해 나중에 쓸 것
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return userId.toString(); }

}

