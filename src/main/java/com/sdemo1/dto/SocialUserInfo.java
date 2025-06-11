package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    private String email;
    private String phone;
    private String name;
    private String picture;
    private String socialId;
    private String provider; // GOOGLE, NAVER, KAKAO 등
} 