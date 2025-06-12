package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.security.JwtTokenProvider;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ck/auth/token")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Value("${jwt.cookie.refresh-token.name}")
    private String refreshTokenCookieName;

    @Value("${jwt.cookie.refresh-token.http-only}")
    private boolean refreshTokenHttpOnly;

    @Value("${jwt.cookie.refresh-token.secure}")
    private boolean refreshTokenSecure;

    @Value("${jwt.cookie.refresh-token.path}")
    private String refreshTokenPath;

    @Value("${jwt.cookie.refresh-token.max-age}")
    private int refreshTokenMaxAge;

    @Value("${jwt.cookie.refresh-token.same-site}")
    private String refreshTokenSameSite;

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<?>> checkAuth(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            log.info("=== 자동 로그인 체크 시작 ===");
            
            if (refreshToken == null) {
                log.info("Refresh Token이 없습니다.");
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
            }

            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.info("유효하지 않은 Refresh Token입니다.");
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
            }

            // Refresh Token에서 사용자 정보 추출
            String memberId = jwtTokenProvider.getUserInfoFromToken(refreshToken);
            log.info("user: {}", memberId);

            // DB에서 사용자 정보 조회
            Member member = memberRepository.findById(Integer.parseInt(memberId))
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 사용자 정보로 AccessToken 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(member);


            Map<String, Object> response = new HashMap<>();
            response.put("isLoggedIn", true);
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("로그인 상태입니다.", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("자동 로그인 체크 실패: {}", e.getMessage());
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("로그인이 필요합니다.", Map.of("isLoggedIn", false), HttpStatus.OK));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            log.info("=== 토큰 재발급 시작 ===");
            
            if (refreshToken == null) {
                log.error("Refresh Token이 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Refresh Token이 없습니다.", null, HttpStatus.UNAUTHORIZED));
            }

            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.error("유효하지 않은 Refresh Token입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("유효하지 않은 Refresh Token입니다.", null, HttpStatus.UNAUTHORIZED));
            }

            // Refresh Token에서 사용자 정보 추출
            String memberId = jwtTokenProvider.getUserInfoFromToken(refreshToken);
            if (memberId == null) {
                log.error("사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("사용자 정보를 찾을 수 없습니다.", null, HttpStatus.UNAUTHORIZED));
            }
            
            log.info("user: {}", memberId);

            // DB에서 사용자 정보 조회
            Member member = memberRepository.findById(Integer.parseInt(memberId))
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 사용자 정보로 AccessToken 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(member);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("토큰 재발급 성공", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("토큰 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("토큰 재발급 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}