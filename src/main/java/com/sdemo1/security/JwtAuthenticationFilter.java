package com.sdemo1.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;
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
            String token = getJwtFromRequest(request);
            
            if (token == null) {
                log.debug("토큰이 없습니다. 다음 필터로 진행합니다.");
                filterChain.doFilter(request, response);
                return;
            }
            
            log.debug("요청 URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
            log.debug("요청 헤더: {}", Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                    name -> name,
                    request::getHeader
                )));
            
            try {
                Authentication authentication = jwtTokenProvider.validateAndGetAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 정보가 SecurityContext에 저장되었습니다: {}", authentication);
            } catch (Exception e) {
                log.warn("유효하지 않은 JWT 토큰입니다");
                String errorDetail = String.format("JWT 토큰 검증 실패: %s", e.getMessage());
                handleErrorResponse(response, HttpStatus.UNAUTHORIZED, errorDetail);
                return;
            }
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 오류 발생", e);
            String errorDetail = String.format("[URI: %s, Method: %s] JWT 필터 처리 중 오류 발생: %s", 
                request.getRequestURI(), 
                request.getMethod(), 
                e.getMessage());
            
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", errorDetail);
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleErrorResponse(HttpServletResponse response, HttpStatus status, String message, Object... args) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        String formattedMessage = String.format(message, args);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", formattedMessage);
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 