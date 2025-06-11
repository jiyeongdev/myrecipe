package com.sdemo1.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseCookie;

@Getter
@Builder
public class RefreshTokenCookieConfig {
    private final String cookieName;
    private final boolean httpOnly;
    private final boolean secure;
    private final String path;
    private final long maxAge;
    private final String sameSite;

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