package com.sdemo1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;

@Configuration
public class RefreshTokenCookieConfig {
    @Value("${jwt.cookie.refresh-token.name}")
    private String cookieName;

    @Value("${jwt.cookie.refresh-token.http-only}")
    private boolean httpOnly;

    @Value("${jwt.cookie.refresh-token.secure}")
    private boolean secure;

    @Value("${jwt.cookie.refresh-token.path}")
    private String path;

    @Value("${jwt.cookie.refresh-token.max-age}")
    private long maxAge;

    @Value("${jwt.cookie.refresh-token.same-site}")
    private String sameSite;

    public ResponseCookie createCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(httpOnly)
                .secure(secure)
                .path(path)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
    }
}
