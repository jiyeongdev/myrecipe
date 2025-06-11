package com.sdemo1.service.impl;

import com.sdemo1.dto.SocialUserInfo;
import com.sdemo1.entity.Member;
import com.sdemo1.entity.MemberSocialAccount;
import com.sdemo1.service.AbstractSocialAuthService;
import com.sdemo1.service.MemberService;
import com.sdemo1.service.MemberSocialAccountService;
import com.sdemo1.util.JwtTokenUtil;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl extends AbstractSocialAuthService {

    private final RestTemplate restTemplate;
    private final MemberService memberService;
    private final MemberSocialAccountService memberSocialAccountService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorizationUri;

    /**
     *  구글서버에 인증 url 요청
     */
    @Override
    public String getAuthorizationUrl(String provider) {
        return authorizationUri + "?" +
                "client_id=" + clientId +
                "&redirect_uri=" + getRedirectUri() +
                "&response_type=code" + 
                "&scope=openid%20profile%20email" ;
    }

    /**
     * 구글서버에 구글토큰 요청
     */
    @Override
    public String getAccessToken(String provider, String authorizationCode) {
        //요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //요청 본문 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", authorizationCode);
        params.add("redirect_uri", getRedirectUri());
        params.add("grant_type", "authorization_code");

        //요청 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        loggingAuthorizationUri(provider, tokenUri, headers, params);

                
        try {

            ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                Map.class
            );
            log.info("Token Response: {}", response.getBody());
            log.info("token Status Code: {}", response.getStatusCode());
            log.info("token Headers: {}", response.getHeaders());
            log.info("token Body: {}", response.getBody());

            
            // 응답에서 액세스 토큰 추출
            String accessToken = (String) response.getBody().get("access_token");
            return accessToken;
            
            
        } catch (Exception e) {
            log.error("=== Google 토큰 요청 실패 상세 정보 ===");
            log.error("에러 메시지: {}", e.getMessage());
            log.error("에러 클래스: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
                log.error("원인 스택 트레이스:", e.getCause());
            }
            log.error("전체 스택 트레이스:", e);

            String errorMessage;
            if (e.getMessage().contains("invalid_grant")) {
                errorMessage = "인증 코드가 만료되었거나 이미 사용되었습니다. 새로운 인증 코드를 요청해주세요.";
            } else if (e.getMessage().contains("redirect_uri_mismatch")) {
                errorMessage = "redirect_uri가 일치하지 않습니다. 설정된 URI: " + getRedirectUri();
            } else {
                errorMessage = "OAuth 토큰 요청 중 오류 발생: " + e.getMessage();
            }
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);  // 원본 예외를 포함하여 다시 던짐
        }
    }

    /**
     * 구글서버에 사용자 정보 요청
     */
    @Override
    public SocialUserInfo getUserInfo(String provider, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            userInfoUri,
            HttpMethod.GET,
            request,
            Map.class
        );
        Map<String, Object> userInfo = response.getBody();

        return new SocialUserInfo(
            (String) userInfo.get("email"),
            (String) userInfo.get("phone"),
            (String) userInfo.get("name"),
            (String) userInfo.get("picture"),
            (String) userInfo.get("sub"),
            MemberSocialAccount.Provider.GOOGLE.getValue()
        );
        } catch (Exception e) {
            log.error("{} 사용자 정보 요청 실패: {}", provider, e.getMessage());
            throw new RuntimeException("{} 사용자 정보 요청 실패", e);
        }
    }

    @Override
    @Transactional
    public SocialLoginResponse createAccessTokenByUserInfo(SocialUserInfo userInfo) {
        log.info("=== {} 사용자 정보 저장/업데이트 시작 ===", userInfo.getProvider());
        log.info("사용자 정보: {}", userInfo);

        try {
            // 기존 소셜 계정 여부 확인
            Optional<MemberSocialAccount> existingAccount = memberSocialAccountService
                .findByProviderAndProviderId(
                    MemberSocialAccount.Provider.fromString(userInfo.getProvider()), 
                    userInfo.getSocialId()
                );

            Member member;
            if (existingAccount.isPresent()) {  
                //기존 회원
                member = existingAccount.get().getMember();

            }else{ 
                //새 회원 기본 정보 생성
                Member newMember = new Member();
                newMember.setRole(Member.Role.USER);
                newMember.setCompleteFlag(false);
                member = memberService.createMember(newMember);
                log.info("새 회원 생성 완료: {}", member);
                log.info("생성된 회원 ID: {}", member.getMemberId());

                // 새 소셜 계정 생성 
                MemberSocialAccount socialAccount = memberSocialAccountService.createSocialAccount(
                member.getMemberId(), 
                userInfo.getEmail(), 
                MemberSocialAccount.Provider.GOOGLE,
                userInfo.getSocialId(), 
                userInfo.getPicture()
            );
            log.info("소셜 계정 정보 저장/업데이트 완료: {}", socialAccount);
            }

            // JWT 토큰 생성
            Map<String, Object> claims = JwtTokenUtil.createUserInfoFromMember(member);
            String token = Jwts.builder()
                .setClaims(claims)
                .signWith(jwtTokenUtil.getKey())
                .compact();

            return new SocialLoginResponse( token, member.getCompleteFlag());
        } catch (Exception e) {
            log.error("사용자 정보 저장/업데이트 실패: {}", e.getMessage());
            throw new RuntimeException("사용자 정보 저장/업데이트 실패", e);
        }
    }
} 