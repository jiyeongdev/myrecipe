package com.sdemo1.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import com.sdemo1.entity.Member;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {

    private final SecretKey key;

    public JwtTokenUtil(
        @Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public SecretKey getKey() {
        return key;
    }

    // JWT 클레임 상수
    public static final class Claims {
        public static final String MEMBER_ID = "memberId";
        public static final String NAME = "name";
        public static final String PHONE = "phone";
        public static final String ROLE = "role";
        public static final String COMPLETE_FLAG = "completeFlag";
        public static final String EMAIL = "email";
    }

    // JWT 클레임 타입 정의
    public enum ClaimType {
        MEMBER_ID(Long.class),
        NAME(String.class),
        PHONE(String.class),
        ROLE(String.class);

        private final Class<?> type;

        ClaimType(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }

    // Member 정보를 JWT 클레임으로 변환
    public static Map<String, Object> createUserInfoFromMember(Member member) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(Claims.MEMBER_ID, member.getMemberId());
        userInfo.put(Claims.NAME, member.getName());
        userInfo.put(Claims.ROLE, member.getRole());
        userInfo.put(Claims.PHONE, member.getPhone());
        userInfo.put(Claims.COMPLETE_FLAG, member.getCompleteFlag());
        return userInfo;
    }

    // JWT 토큰에서 특정 클레임 값 추출
    public  <T> T getClaimFromToken(String token, String claimName, Class<T> type) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(claimName, type);
    }

    // JWT 토큰에서 사용자 ID 추출
    public  String getMemberIdFromToken(String token) {
        return getClaimFromToken(token, Claims.MEMBER_ID, String.class);
    }

    // JWT 토큰에서 역할 추출
    public  String getRoleFromToken(String token) {
        return getClaimFromToken(token, Claims.ROLE, String.class);
    }

    // JWT 토큰에서 전화번호 추출
    public  String getPhoneFromToken(String token) {
        return getClaimFromToken(token, Claims.PHONE, String.class);
    }

    // JWT 토큰에서 이름 추출
    public  String getNameFromToken(String token) {
        return getClaimFromToken(token, Claims.NAME, String.class);
    }

} 