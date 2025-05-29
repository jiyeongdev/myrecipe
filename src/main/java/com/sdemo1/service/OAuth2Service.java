package com.sdemo1.service;

import com.sdemo1.dto.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2AuthorizedClientService clientService;

    public OAuth2UserInfo getGoogleUserInfo() {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oauth2User = authentication.getPrincipal();
        
        return OAuth2UserInfo.builder()
                .email(oauth2User.getAttribute("email"))
                .name(oauth2User.getAttribute("name"))
                .picture(oauth2User.getAttribute("picture"))
                .build();
    }
} 