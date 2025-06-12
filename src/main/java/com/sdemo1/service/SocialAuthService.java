package com.sdemo1.service;

import com.sdemo1.dto.SocialUserInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public interface SocialAuthService {
    String getAuthorizationUrl(String provider);
    String getAccessToken(String provider, String code);
    SocialUserInfo getUserInfo(String provider, String accessToken);
    SocialLoginResponse createAccessTokenByUserInfo(SocialUserInfo userInfo);
    String decodeAuthorizationCode(String code);
    void setRedirectUri(String redirectUri);
    String getRedirectUri();
    void clearRedirectUri();
    void loggingAuthorizationUri(String provider, String tokenUri, HttpHeaders headers, MultiValueMap<String, String> params);
    
    record SocialLoginResponse(String token, String refreshToken, boolean completeFlag, int memberId) {}
} 