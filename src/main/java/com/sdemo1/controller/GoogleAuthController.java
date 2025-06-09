package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.Member;
import com.sdemo1.exception.CustomException;
import com.sdemo1.security.JwtTokenProvider;
import com.sdemo1.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/ck/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    private String decodeAuthorizationCode(String code) {
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

    private String extractCodeFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "code".equals(pair[0])) {
                        return pair[1];
                    }
                }
            }
        } catch (Exception e) {
            log.error("URL에서 code 파라미터 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    @GetMapping("/url")
    public ResponseEntity<Void> getGoogleAuthUrl() {
        try {
            log.info("=== Google OAuth 인증 URL 리다이렉트 시작 ===");
            String authUrl = googleAuthService.getGoogleAuthUrl();
            log.info("리다이렉트 URL: {}", authUrl);
            
            if (authUrl == null || authUrl.trim().isEmpty()) {
                log.error("생성된 인증 URL이 유효하지 않습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authUrl))
                    .build();
        } catch (Exception e) {
            log.error("Google OAuth URL 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/url-str")
    public ApiResponse<?> getGoogleAuthUrlStr() {
        log.info("=== Google OAuth 인증 URL 생성 시작 ===");
        String authUrl = googleAuthService.getGoogleAuthUrl();
        String code = extractCodeFromUrl(authUrl);        
        log.info("생성된 인증 URL: {}", authUrl);
        log.info("구글인가코드: {}", code);
        return new ApiResponse<>("Google 인증 URL 생성 완료", authUrl, HttpStatus.OK);
    }

    @PostMapping("/token")
    public ApiResponse<?> getGoogleToken(@RequestBody Map<String, String> request) {
        try {
            log.info("=== Google OAuth 토큰 요청 시작 ===");
            String code1 = request.get("code");
            String code = decodeAuthorizationCode(code1);
            log.info("인가코드: {}", code);

            // Google Access Token 요청
            String googleAccessToken = googleAuthService.getGoogleAccessToken(code);
            log.info("Google Access Token: {}", googleAccessToken);

            // Google 사용자 정보 요청
            Map<String, Object> userInfo = googleAuthService.getGoogleUserInfo(googleAccessToken);
            log.info("Google 사용자 정보: {}", userInfo);

            // 사용자 정보 저장/업데이트
            Map<String, Object> result = googleAuthService.saveOrUpdateGoogleUser(userInfo);
            Member member = (Member) result.get("member");
            boolean isNewUser = (boolean) result.get("isNewUser");
            Map<String, Object> tokenUserInfo = (Map<String, Object>) result.get("userInfo");

            // JWT 토큰 생성
            String jwtAccessToken = jwtTokenProvider.createAccessToken(tokenUserInfo);
            String jwtRefreshToken = jwtTokenProvider.createRefreshToken(tokenUserInfo);
            log.info("JWT 토큰 생성 완료");

            Map<String, Object> response = new HashMap<>();
            response.put("member", member);
            response.put("isNewUser", isNewUser);
            response.put("accessToken", jwtAccessToken);
            response.put("refreshToken", jwtRefreshToken);

            return new ApiResponse<>("Google 로그인 성공", response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Google OAuth 토큰 요청 실패: {}", e.getMessage(), e);
            return new ApiResponse<>("Google 로그인 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 