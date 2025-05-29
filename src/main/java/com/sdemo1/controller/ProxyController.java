package com.sdemo1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    @RequestMapping("/**")
    public RedirectView proxyRequest(HttpServletRequest request) {
        String originalPath = request.getRequestURI();
        String queryString = request.getQueryString();
        
        // /api/proxy/ 제거하고 실제 경로만 추출
        String targetPath = originalPath.substring("/api/proxy".length());
        
        // 쿼리스트링이 있으면 추가
        String targetUrl = queryString != null ? 
            targetPath + "?" + queryString : 
            targetPath;
            
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(targetUrl);
        return redirectView;
    }
} 