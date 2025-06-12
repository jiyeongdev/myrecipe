package com.sdemo1.controller;

import com.sdemo1.dto.SocialUserInfo;
import com.sdemo1.entity.MemberSocialAccount;
import com.sdemo1.service.SocialAuthService;
import com.sdemo1.service.SocialAuthService.SocialLoginResponse;
import com.sdemo1.config.RefreshTokenCookieConfig;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ck/auth")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;
    private final RefreshTokenCookieConfig refreshTokenCookieConfig;


    @GetMapping("/{provider}/auth-url")
    public ResponseEntity<ApiResponse<?>> getAuthorizationUrl(@PathVariable("provider") String provider , @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        try {
           // provider 유효성 검사
           ResponseEntity<ApiResponse<?>> validationResponse = validateProvider(provider);
           if (validationResponse != null) {
               return validationResponse;
           }

            log.info("=== Google OAuth 인증 URL 리다이렉트 시작 ===");
            socialAuthService.setRedirectUri(redirectUri);
            log.info("현재 Redirect URI: {}", socialAuthService.getRedirectUri());

            String authUrl = socialAuthService.getAuthorizationUrl(provider);

            log.info("{} 인가코드 생성 URL: {}", provider, authUrl);
            
            if (authUrl == null || authUrl.trim().isEmpty()) {
                log.error("생성된 인증 URL이 유효하지 않습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(authUrl))
                    .build();
        
        } catch (Exception e) {
            log.error("{} 인증 URL 생성 실패: {}", provider, e.getMessage(), e);
            return new ResponseEntity<>(new ApiResponse<>("인증 URL 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /** 디버깅용 - auth url string  */
    @GetMapping("/{provider}/auth-url-str")
    public ApiResponse<?> getGoogleAuthUrlStr(@PathVariable("provider") String provider , @RequestParam(value = "redirect_uri", required = false) String redirectUri) {

        socialAuthService.setRedirectUri(redirectUri);
        log.info("현재 Redirect URI: {}", socialAuthService.getRedirectUri());

        String authUrl = socialAuthService.getAuthorizationUrl(provider);
        log.info("{} 생성된 인증 URL: {}", provider, authUrl);
        return new ApiResponse<>("인증 URL 생성 완료", authUrl, HttpStatus.OK);
    }

    @PostMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<?>> getSocialToken(
            @PathVariable("provider") String provider,
            @RequestBody Map<String, String> request) {
        try {

            // provider 유효성 검사
            ResponseEntity<ApiResponse<?>> validationResponse = validateProvider(provider);
            if (validationResponse != null) {
                return validationResponse;
            }

            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("인가 코드가 필요합니다."));
            }

        

            log.info("=== {} OAuth 토큰 요청 시작 ===", provider.toUpperCase());
            code = socialAuthService.decodeAuthorizationCode(code);
            log.info("인가코드: {}", code);

            // 소셜 Access Token 요청
            String accessToken = socialAuthService.getAccessToken(provider, code);
            log.info("{} Access Token: {}", provider, accessToken);

            // 소셜 사용자 정보 요청
            SocialUserInfo userInfo = socialAuthService.getUserInfo(provider, accessToken);
            log.info("{} 사용자 정보: {}", provider, userInfo);

            // 사용자 정보 확인 또는 생성 후 accessToken 생성
            SocialLoginResponse response = socialAuthService.createAccessTokenByUserInfo(userInfo);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("memberId", response.memberId());
            responseMap.put("completeFlag", response.completeFlag());
            responseMap.put("accessToken", response.token());
            
            // Refresh Token 쿠키 생성 후 헤더에 추가
            ResponseCookie refreshTokenCookie = refreshTokenCookieConfig.createCookie(response.refreshToken());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new ApiResponse<>(provider + " 로그인 성공", responseMap, HttpStatus.OK));
        } catch (Exception e) {
            log.error("소셜 로그인 토큰 요청 실패: {}", e.getMessage());
            throw new CustomException("소셜 로그인 처리 중 오류가 발생했습니다.", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            socialAuthService.clearRedirectUri();
        }
    }

    private ResponseEntity<ApiResponse<?>> validateProvider(String provider) {
        try {
            MemberSocialAccount.Provider.fromString(provider);
            return null;
        } catch (IllegalArgumentException e) {
            log.error("존재하지 않는 소셜 로그인 제공자: {}", provider);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("존재하지 않는 소셜 로그인 제공자입니다: " + provider));
        }
    }

} 