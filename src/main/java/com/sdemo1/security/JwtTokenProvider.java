package com.sdemo1.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String adminToken;
    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final MemberRepository memberRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds,
            @Value("${jwt.admin-token}") String adminToken,
            MemberRepository memberRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.adminToken = adminToken;
        this.memberRepository = memberRepository;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Map<String, Object> userInfo) {
        log.info("=== Access Token 생성 시작 ===");
        return createToken(userInfo, accessTokenValidityInMilliseconds);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Map<String, Object> userInfo) {
        log.info("=== Refresh Token 생성 시작 ===");
        return createToken(userInfo, refreshTokenValidityInMilliseconds);
    }

    /**
     * JWT 토큰 생성
     */
    private String createToken(Map<String, Object> userInfo, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String userLoginId = getUserLoginId(userInfo);
        Member.Role userRole = getUserRole(userLoginId);

        return Jwts.builder()
                .setSubject(userLoginId)
                .claim("email", userInfo.get("email"))
                .claim("sub", userInfo.get("sub"))
                .claim("auth", userRole.name())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 사용자 로그인 ID 생성
     */
    private String getUserLoginId(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("email");
        String sub = (String) userInfo.get("sub");
        return (email != null && !email.isEmpty()) ? email : "google_" + sub;
    }

    /**
     * 사용자 권한 조회
     */
    private Member.Role getUserRole(String userLoginId) {
        return memberRepository.findByUserLoginId(userLoginId)
                .map(Member::getRole)
                .orElse(Member.Role.USER);
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            if (isAdminToken(token)) {
                return true;
            }
            
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 관리자 토큰 여부 확인
     */
    private boolean isAdminToken(String token) {
        return adminToken != null && !adminToken.isEmpty() && adminToken.equals(token);
    }

    /**
     * JWT 토큰으로 인증 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);
        Collection<? extends GrantedAuthority> authorities = getAuthoritiesFromClaims(claims);

        return new UsernamePasswordAuthenticationToken(
            claims.getSubject(),   // 사용자 식별자 (email 또는 google_sub)
            "",                    // credentials (JWT에서는 비밀번호가 필요 없으므로 빈 문자열)
            authorities            // 권한 정보
        );
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims getClaimsFromToken(String token) {
        if (isAdminToken(token)) {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(adminToken)
                    .getBody();
        }
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Claims에서 권한 정보 추출
     */
    private Collection<? extends GrantedAuthority> getAuthoritiesFromClaims(Claims claims) {
        return Arrays.stream(claims.get("auth", String.class).split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * 토큰에서 사용자 식별자 추출
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
} 