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

    @GetMapping("/url-redirect")
    public ResponseEntity<Void> getGoogleAuthUrl2() {
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

    @GetMapping("/url")
    public ApiResponse<?> getGoogleAuthUrl() {
        log.info("=== Google OAuth 인증 URL 생성 시작 ===");
        String authUrl = googleAuthService.getGoogleAuthUrl();
        String code = extractCodeFromUrl(authUrl);        
        log.info("생성된 인증 URL: {}", authUrl);
        log.info("구글인가코드: {}", code);
        return new ApiResponse<>("Google 인증 URL 생성 완료", authUrl, HttpStatus.OK);
    }

    @PostMapping("/token")
    public ApiResponse<?> getGoogleToken(@RequestBody Map<String, String> request) {
        log.info("=== Google OAuth 토큰 요청 시작 ===");
        log.info("요청 데이터: {}", request);
        
        try {
            String code = request.get("code");
            String decodedCode = decodeAuthorizationCode(code);
            
            // Google OAuth2 토큰 획득
            String accessToken = googleAuthService.getGoogleAccessToken(decodedCode);
            log.info("Google Access Token 획득 성공");
            
            // Google 사용자 정보 획득
            Map<String, Object> userInfo = googleAuthService.getGoogleUserInfo(accessToken);
            log.info("사용자 정보: {}", userInfo);

            // 사용자 정보 저장 또는 업데이트
            Member member = googleAuthService.saveOrUpdateGoogleUser(userInfo);
            log.info("사용자 정보 저장/업데이트 완료: {}", member.getUserLoginId());
            
            // JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createAccessToken(userInfo);
            log.info("JWT 토큰 생성 완료 : {}", jwtToken);
            
            return new ApiResponse<>("JWT 토큰 생성 완료!", jwtToken, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Google OAuth 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(
                "인증 처리 중 오류가 발생했습니다",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
        }
    }
} 