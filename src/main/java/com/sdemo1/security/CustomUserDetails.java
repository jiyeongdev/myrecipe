package com.sdemo1.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sdemo1.entity.Member;
import com.sdemo1.entity.MemberSocialAccount;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {
    
    private final Member member;
    private final MemberSocialAccount socialAccount;

    public CustomUserDetails(Member member, MemberSocialAccount socialAccount) {
        this.member = member;
        this.socialAccount = socialAccount;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole()));
    }

    @Override
    public String getPassword() {
        return socialAccount.getUserLoginPw();
    }

    @Override
    public String getUsername() {
        return socialAccount.getUserLoginId();
    }

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