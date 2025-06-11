package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ck/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.cookie.refresh-token.name}")
    private String refreshTokenCookieName;

    @Value("${jwt.cookie.refresh-token.http-only}")
    private boolean refreshTokenHttpOnly;

    @Value("${jwt.cookie.refresh-token.secure}")
    private boolean refreshTokenSecure;

    @Value("${jwt.cookie.refresh-token.path}")
    private String refreshTokenPath;

    @Value("${jwt.cookie.refresh-token.max-age}")
    private int refreshTokenMaxAge;

    @Value("${jwt.cookie.refresh-token.same-site}")
    private String refreshTokenSameSite;

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<?>> checkAuth(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            log.info("=== 자동 로그인 체크 시작 ===");
            
            if (refreshToken == null) {
                log.info("Refresh Token이 없습니다.");
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
            }

            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.info("유효하지 않은 Refresh Token입니다.");
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
            }

            // Refresh Token이 유효하면 새로운 Access Token 발급
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", username);

            String newAccessToken = jwtTokenProvider.createAccessToken(userInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("isLoggedIn", true);
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("로그인 상태입니다.", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("자동 로그인 체크 실패: {}", e.getMessage());
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            log.info("=== 토큰 재발급 시작 ===");
            
            if (refreshToken == null) {
                log.error("Refresh Token이 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Refresh Token이 없습니다.", null, HttpStatus.UNAUTHORIZED));
            }

            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.error("유효하지 않은 Refresh Token입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("유효하지 않은 Refresh Token입니다.", null, HttpStatus.UNAUTHORIZED));
            }

            // Refresh Token에서 사용자 정보 추출
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", username);

            // 새로운 Access Token만 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(userInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("토큰 재발급 성공", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("토큰 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("토큰 재발급 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}