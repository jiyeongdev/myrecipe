package com.sdemo1.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdemo1.common.response.ApiResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // JWT 토큰이 필요하지 않은 경로들
        return path.startsWith("/ck/auth/") ||
               path.equals("/health-check");
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

            try {
                Authentication authentication = jwtTokenProvider.validateAndGetAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
            } catch (JwtTokenProvider.TokenValidationException e) {
                handleErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage());
                return;
            }

            filterChain.doFilter(request, response);
            log.info("=== JWT 필터 종료 ===");
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 오류 발생", e);
            handleErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<Void> apiResponse = new ApiResponse<>(message, null, status);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
} 