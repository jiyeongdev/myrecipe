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
import com.sdemo1.util.JwtTokenUtil;

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

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds,
            @Value("${jwt.admin-token-validity-in-seconds:31536000}") long adminTokenValidityInSeconds,
            @Value("${jwt.admin-token}") String adminToken,
            MemberRepository memberRepository) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.adminToken = adminToken;
    }

    /**
     * 현재 시간을 한국 시간으로 변환
     */
    private Date getCurrentKoreaTime() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 만료 시간을 한국 시간으로 계산
     */
    private Date calculateExpirationTime(long validityInMilliseconds) {
        return new Date(System.currentTimeMillis() + validityInMilliseconds);
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Member member) {
        Map<String, Object> claims = JwtTokenUtil.createUserInfoFromMember(member);
        return createAccessToken(claims, accessTokenValidityInMilliseconds);
    }

    /**
     * memberId 값만 포함한 Refresh Token 생성
     */
    public String createRefreshToken(Member member) {
        return createRefreshToken(String.valueOf(member.getMemberId()), refreshTokenValidityInMilliseconds);
    }

    /**
     * Access Token 생성 (내부용)
     */
    private String createAccessToken(Map<String, Object> claims, long validityInMilliseconds) {
        Date now = getCurrentKoreaTime();
        Date validity = calculateExpirationTime(validityInMilliseconds);
        String memberId = String.valueOf(claims.get("memberId"));

        log.info("Access Token 생성 - 발급 시간: {}, 만료 시간: {}, 유효 기간: {}초", 
            now, validity, validityInMilliseconds / 1000);

        return Jwts.builder()
                .setSubject(memberId)
                .claim("memberId", claims.get("memberId"))
                .claim("name", claims.get("name"))
                .claim("role", claims.get("role"))
                .claim("phone", claims.get("phone"))
                .claim("completeFlag", claims.get("completeFlag"))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Refresh Token 생성 (내부용)
     */
    private String createRefreshToken(String memberId, long validityInMilliseconds) {
        Date now = getCurrentKoreaTime();
        Date validity = calculateExpirationTime(validityInMilliseconds);

        log.info("Refresh Token 생성 - 발급 시간: {}, 만료 시간: {}, 유효 기간: {}초", 
            now, validity, validityInMilliseconds / 1000);

        return Jwts.builder()
                .setSubject(memberId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * JWT 토큰 유효성 검증 및 인증 처리
     * @return Authentication 인증 객체 (유효한 토큰인 경우)
     * @throws TokenValidationException 토큰이 유효하지 않은 경우
     */
    public Authentication validateAndGetAuthentication(String token) {
        try {
            if (isAdminToken(token)) {
                return getAuthentication(token);
            }
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            log.info("토큰 검증 - 현재 시간: {}, 만료 시간: {}", 
                getCurrentKoreaTime(), claims.getExpiration());
                
            return getAuthentication(token);
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다. 만료 시간: {}, 현재 시간: {}", 
                e.getClaims().getExpiration(), getCurrentKoreaTime());
            throw new TokenValidationException(TokenError.EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            throw new TokenValidationException(TokenError.UNSUPPORTED);
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 JWT 토큰입니다: {}", e.getMessage());
            throw new TokenValidationException(TokenError.MALFORMED);
        } catch (SignatureException e) {
            log.error("JWT 토큰의 서명이 유효하지 않습니다: {}", e.getMessage());
            throw new TokenValidationException(TokenError.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
            throw new TokenValidationException(TokenError.EMPTY);
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예상치 못한 에러가 발생했습니다: {}", e.getMessage());
            throw new TokenValidationException(TokenError.UNKNOWN);
        }
    }

    /**
     * 토큰 검증 예외 클래스
     */
    public static class TokenValidationException extends RuntimeException {
        private final TokenError error;

        public TokenValidationException(TokenError error) {
            super(error.getMessage());
            this.error = error;
        }

        public TokenError getError() {
            return error;
        }
    }

    /**
     * 토큰 에러 타입을 정의하는 enum
     */
    public enum TokenError {
        EXPIRED("토큰이 만료되었습니다"),
        UNSUPPORTED("지원하지 않는 토큰입니다"),
        MALFORMED("잘못된 형식의 토큰입니다"),
        INVALID_SIGNATURE("유효하지 않은 서명입니다"),
        EMPTY("토큰이 비어있습니다"),
        UNKNOWN("알 수 없는 에러가 발생했습니다");

        private final String message;

        TokenError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
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
        log.info("JWT 토큰 subject: {}", claims.getSubject());
        
        return new UsernamePasswordAuthenticationToken(
            claims.getSubject(),   // 사용자 식별자 (memberId)
            "",        // credentials (JWT에서는 비밀번호가 필요 없으므로 빈 문자열)
            getAuthoritiesFromClaims(claims) // 권한 정보
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
     * Claims에서 "role" 키 사용해서,
     * 
     * JWT 토큰의 클레임(claims)에서 사용자의 권한 정보를 추출
     * Spring Security의 GrantedAuthority 형태로 변환
     */
    private Collection<? extends GrantedAuthority> getAuthoritiesFromClaims(Claims claims) {
        try {
            // 1. "role" 키에서 권한 정보를 가져옴
            String auth = claims.get("role", String.class);

            // 2. 권한이 없으면 기본값 "ROLE_USER" 부여
            if (auth == null || auth.isEmpty()) {
                return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            }
            // 3. 권한 문자열을 쉼표(,)로 분리하고 각각을 GrantedAuthority로 변환
            return Arrays.stream(auth.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 4. 에러 발생 시 기본 권한 부여
            log.error("권한 정보 추출 중 오류 발생: {}", e.getMessage());
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    /**
     * 토큰에서 사용자 식별자 추출
     */
    public String getUserInfoFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
} 