package com.sdemo1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        /** 배지영
         *  String path = request.getRequestURI();
        // JWT 토큰이 필요하지 않은 경로들
        return path.startsWith("/auth/") || 
               path.startsWith("/login/") ||
               path.startsWith("/ck/auth/") ||
               path.equals("/health-check");
         */
        return true;  // 모든 요청에 대해 필터링하지 않음
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("=== JWT 필터 시작 ===");
            log.info("요청 URI: {}", request.getRequestURI());
            log.info("요청 메서드: {}", request.getMethod());
            log.info("요청 헤더: {}", Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                    headerName -> headerName,
                    request::getHeader
                )));

            String token = resolveToken(request);
            log.info("JWT 토큰 추출: {}", token);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            } else {
                log.info("토큰이 없거나 유효하지 않습니다");
            }

            filterChain.doFilter(request, response);
            log.info("=== JWT 필터 종료 ===");
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 오류 발생", e);
            throw e;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 