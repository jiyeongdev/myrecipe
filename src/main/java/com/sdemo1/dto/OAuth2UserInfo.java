package com.sdemo1.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String name;
    private String picture;
} 