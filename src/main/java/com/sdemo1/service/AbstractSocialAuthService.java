package com.sdemo1.service;

import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;

import org.springframework.http.HttpHeaders;

@Slf4j
public abstract class AbstractSocialAuthService implements SocialAuthService {
        
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    protected String redirectUri;
    
    private String currentRedirectUri;

    @Override
    public void setRedirectUri(String newRedirectUri) {
        if (newRedirectUri != null && !newRedirectUri.trim().isEmpty()) {
            this.currentRedirectUri = newRedirectUri;
            log.info("Redirect URI가 변경되었습니다: {}", this.currentRedirectUri);
        } else {
            this.currentRedirectUri = this.redirectUri;
            log.info("Redirect URI가 기본값으로 재설정되었습니다: {}", this.currentRedirectUri);
        }
    }

    @Override
    public String getRedirectUri() {
        return currentRedirectUri;
    }

    /**
     * 디코딩
     * @param code
     * @return
     */
    @Override
    public String decodeAuthorizationCode(String code) {
        log.info("원본 인증 코드: {}", code);
        try {
            String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
            log.info("디코딩된 인증 코드: {}", decodedCode);
            return decodedCode;
        } catch (Exception e) {
            log.error("인증 코드 디코딩 실패: {}", e.getMessage());
            throw new RuntimeException("인증 코드 디코딩 실패", e);
        }
    }

    @Override
    public void loggingAuthorizationUri(String provider, String tokenUri, HttpHeaders headers, MultiValueMap<String, String> params) {
        
          // 요청 정보 상세 로깅
          log.info("=== 토큰 요청 상세 정보 ===");
          log.info("요청 URL: {}", tokenUri);
          log.info("요청 헤더: {}", headers);
          log.info("요청 바디: {}", params);
          log.info("최종 요청 URL (바디 포함): {}?{}", tokenUri, 
          params.entrySet().stream()
                  .map(entry -> entry.getKey() + "=" + entry.getValue().get(0))
                  .reduce((a, b) -> a + "&" + b)
                  .orElse(""));
    }
    
} 