package com.sdemo1.service;

import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;

    public String getGoogleAuthUrl() {
        try {
            log.info("=== Google OAuth URL 생성 시작 ===");
            log.info("Client ID: {}", clientId);
            log.info("Redirect URI: {}", redirectUri);
            
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth";
            String scope = "openid%20profile%20email";  // 공백을 %20으로 인코딩
            
            String url = String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                authUrl,
                clientId,
                redirectUri,
                scope
            );
            
            log.info("생성된 URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Google OAuth URL 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Google OAuth URL 생성 실패", e);
        }
    }

    public String getGoogleAccessToken(String authorizationCode) {
        log.info("=== Google OAuth 토큰 요청 시작 ===");
        log.info("=== Client ID: {}", clientId);
        log.info("=== Client Secret: {}", clientSecret);
        log.info("=== Redirect URI: {}", redirectUri);
        log.info("=== Authorization Code: {}", authorizationCode);
        

        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // 요청 본문 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", "9472329979-3t1bldmdhhbc01pfvs4f1qgjqbn7n405.apps.googleusercontent.com");
        body.add("client_secret", "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");
        body.add("redirect_uri", "https://fridgepal.life/api/google-callback");
        body.add("grant_type", "authorization_code");
        // body.add("client_id", clientId);
        // body.add("client_secret", clientSecret);
        // body.add("redirect_uri", redirectUri);
        // body.add("grant_type", "authorization_code");

        
        // 요청 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        // 요청 정보 상세 로깅
        log.info("=== 토큰 요청 상세 정보 ===");
        log.info("요청 URL: {}", tokenUrl);
        log.info("요청 헤더: {}", headers);
        log.info("요청 바디: {}", body);
        log.info("최종 요청 URL (바디 포함): {}?{}", tokenUrl, 
            body.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue().get(0))
                .reduce((a, b) -> a + "&" + b)
                .orElse(""));
        
        try {
            //토큰 요청
            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                Map.class
            );
            log.info("Token Response: {}", response.getBody());                    log.info("token Status Code: {}", response.getStatusCode());
            log.info("token Headers: {}", response.getHeaders());
            log.info("token Body: {}", response.getBody());

            
            // 응답에서 액세스 토큰 추출
            String accessToken = (String) response.getBody().get("access_token");
            log.info("Google OAuth 토큰 획득 성공");
            return accessToken;
            
        } catch (Exception e) {
            String errorMessage;
            if (e.getMessage().contains("invalid_grant")) {
                errorMessage = "인증 코드가 만료되었거나 이미 사용되었습니다. 새로운 인증 코드를 요청해주세요.";
                log.error(errorMessage);
            } else if (e.getMessage().contains("redirect_uri_mismatch")) {
                errorMessage = "redirect_uri 이 일치하지 않습니다. 확인해주세요.";
                log.error(errorMessage);
            } else {
                errorMessage = "Google OAuth 토큰 요청 중 알 수 없는 오류: " + e.getMessage();
                log.error(errorMessage, e);
            }
            throw new RuntimeException(errorMessage);
        }
    }

    public Map<String, Object> getGoogleUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<?> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                Map.class
            );
            log.info("Google 사용자 정보 획득 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("Google 사용자 정보 요청 실패: {}", e.getMessage());
            throw new RuntimeException("Google 사용자 정보 요청 실패", e);
        }
    }

    public Member saveOrUpdateGoogleUser(Map<String, Object> userInfo) {
        log.info("=== Google 사용자 정보 저장/업데이트 시작 ===");
        log.info("사용자 정보: {}", userInfo);

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");
        String sub = (String) userInfo.get("sub"); // Google의 고유 ID

        // 이메일이 없는 경우 sub을 사용
        final String userLoginId = (email == null || email.trim().isEmpty()) ? "google_" + sub : email;
        final String userName = (name != null) ? name : "Google User";

        return memberRepository.findByUserLoginId(userLoginId)
                .orElseGet(() -> {
                    log.info("새로운 사용자 생성: {}", userLoginId);
                    Member newMember = new Member();
                    newMember.setUserLoginId(userLoginId);
                    newMember.setName(userName);
                    newMember.setEmail(userLoginId);
                    newMember.setProvider(Member.Provider.GOOGLE);
                    newMember.setProviderId(sub);
                    newMember.setProfileImg(picture);
                    newMember.setUserLoginPw("OAUTH2_USER");
                    return memberRepository.save(newMember);
                });
    }
} 