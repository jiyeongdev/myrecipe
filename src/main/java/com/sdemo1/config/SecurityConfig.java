package com.sdemo1.config;

import com.sdemo1.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== Security Filter Chain 설정 시작 ===");
        
        http
            .cors(cors -> {
                log.info("CORS 설정 적용");
                cors.configurationSource(corsConfigurationSource());
            })
            .csrf(csrf -> {
                log.info("CSRF 비활성화");
                csrf.disable();
            })
            .sessionManagement(session -> {
                log.info("세션 정책: STATELESS");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(auth -> {
                log.info("인증 요구사항 설정");
                auth.anyRequest().permitAll();  // 모든 요청 허용

                //배지영
                //auth.requestMatchers("/", "/login/**", "/health-check", "/ck/auth/**").permitAll()
                //.anyRequest().authenticated();
            })
            .exceptionHandling(exception -> {
                log.info("예외 처리 설정");
                exception.authenticationEntryPoint((request, response, authException) -> {
                    log.error("인증 실패: {}", authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    String errorMessage = String.format(
                        "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                        authException.getMessage(),
                        request.getRequestURI()
                    );
                    response.getWriter().write(errorMessage);
                });
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("=== Security Filter Chain 설정 완료 ===");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("=== CORS 설정 시작 ===");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // 개발 환경
            "https://fridgepal.life",     
            "https://www.fridgepal.life"  // 배포된 프론트엔드 도메인
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);  // preflight 요청 캐시 시간 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        log.info("=== CORS 설정 완료 ===");
        return source;
    }
} 