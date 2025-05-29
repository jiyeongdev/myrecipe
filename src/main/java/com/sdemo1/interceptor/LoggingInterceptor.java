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
        String method = request.getMethod();
        String queryString = request.getQueryString();
        
        String actualPath = requestURI.replace("/api/proxy", "");
        
        String fullUrl = queryString != null ? 
            actualPath + "?" + queryString : 
            actualPath;
            
        log.info("[API Request] {} {}", method, fullUrl);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        
        String actualPath = requestURI.replace("/api/proxy", "");
        
        log.info("[API Response] {} {} - Status: {}", method, actualPath, status);
    }
} 