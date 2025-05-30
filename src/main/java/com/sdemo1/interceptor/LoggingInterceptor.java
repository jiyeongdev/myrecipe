package com.sdemo1.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        
        // /health-check 경로는 로깅하지 않음
        if (requestURI.equals("/health-check")) {
            return true;
        }
        
        String method = request.getMethod();
        String queryString = request.getQueryString();
        
        String fullUrl = queryString != null ? 
        requestURI + "?" + queryString : 
        requestURI;
            
        log.info("[API Request] {} {}", method, fullUrl);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        String requestURI = request.getRequestURI();
        
        // /health-check 경로는 로깅하지 않음
        if (requestURI.equals("/health-check")) {
            return;
        }
        
        String method = request.getMethod();
        int status = response.getStatus();
        
        log.info("[API Response] {} {} - Status: {}", method, requestURI, status);
    }
} 