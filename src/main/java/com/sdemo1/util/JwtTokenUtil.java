package com.sdemo1.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.crypto.SecretKey;
import com.sdemo1.entity.Member;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
@Slf4j
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



    /**
     * JWT 토큰에서 사용자 ID를 안전하게 추출하는 메소드
     * @return 사용자 ID
     * @throws SecurityException 인증 정보가 없거나 잘못된 경우
     * @throws NumberFormatException 사용자 ID가 숫자가 아닌 경우
     */public Integer extractMemberIdFromAuth() {
        try {
            // 1. SecurityContext에서 Authentication 객체 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // 2. Authentication 객체 null 체크
            if (auth == null) {
                log.error("Authentication 객체가 null입니다");
                throw new SecurityException("인증 정보가 없습니다");
            }
            
            // 3. 인증 여부 확인
            if (!auth.isAuthenticated()) {
                log.error("인증되지 않은 사용자입니다");
                throw new SecurityException("인증되지 않은 사용자입니다");
            }
            
            // 4. principal 확인
            Object principal = auth.getPrincipal();
            if (principal == null) {
                log.error("Principal이 null입니다");
                throw new SecurityException("인증 정보가 올바르지 않습니다");
            }
            
            // 5. 사용자 이름(ID) 추출
            String memberIdStr = auth.getName();
            if (memberIdStr == null || memberIdStr.trim().isEmpty()) {
                log.error("사용자 ID가 비어있습니다. Principal: {}", principal);
                throw new SecurityException("사용자 ID를 찾을 수 없습니다");
            }
            
            // 6. 숫자로 변환
            try {
                Integer memberId = Integer.parseInt(memberIdStr.trim());
                
                if (memberId <= 0) {
                    log.error("사용자 ID가 유효하지 않습니다: {}", memberId);
                    throw new SecurityException("유효하지 않은 사용자 ID입니다");
                }
                
                log.debug("사용자 ID 추출 성공: {}", memberId);
                return memberId;
                
            } catch (NumberFormatException e) {
                log.error("사용자 ID 형식이 잘못되었습니다: '{}', 오류: {}", memberIdStr, e.getMessage());
                throw new NumberFormatException("사용자 ID가 숫자 형식이 아닙니다: " + memberIdStr);
            }
            
        } catch (SecurityException | NumberFormatException e) {
            // 이미 처리된 예외는 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("사용자 ID 추출 중 예상치 못한 오류 발생", e);
            throw new SecurityException("인증 정보 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
} 