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

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final MemberRepository memberRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds,
            MemberRepository memberRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.memberRepository = memberRepository;
    }

    // Access Token 생성
    public String createAccessToken(Map<String, Object> userInfo) {
        log.info("=== Access Token 생성 시작 ===");
        return createToken(userInfo, accessTokenValidityInMilliseconds);
    }

    // Refresh Token 생성
    public String createRefreshToken(Map<String, Object> userInfo) {
        log.info("=== Refresh Token 생성 시작 ===");
        return createToken(userInfo, refreshTokenValidityInMilliseconds);
    }

    private String createToken(Map<String, Object> userInfo, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String email = (String) userInfo.get("email");
        String sub = (String) userInfo.get("sub");
        String userLoginId = (email != null && !email.isEmpty()) ? email : "google_" + sub;

         // Member 객체를 가져와서 권한 정보를 토큰에 포함
        Member member = memberRepository.findByUserLoginId(userLoginId)
        .orElseThrow(() -> new RuntimeException("User not found"));

        // role이 null인 경우 기본값 설정
        Member.Role userRole = member.getRole() != null ? member.getRole() : Member.Role.USER;
    
        return Jwts.builder()
                .setSubject(userLoginId)
                .claim("email", email)
                .claim("sub", sub)
                .claim("auth", userRole.name())  // 안전한 권한 정보 포함
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // JWT 토큰으로 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth", String.class).split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // JWT 토큰에서 사용자 정보와 권한 정보를 추출하여 인증 객체 생성
        return new UsernamePasswordAuthenticationToken(
            claims.getSubject(),   // 사용자 식별자 (email 또는 google_sub)
            "",  // credentials (JWT에서는 비밀번호가 필요 없으므로 빈 문자열)
            authorities// 권한 정보
             );
    }
     // 토큰에서 사용자 식별자 추출
     public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
} 