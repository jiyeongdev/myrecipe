package com.sdemo1.controller;

import com.sdemo1.dto.OAuth2UserInfo;
import com.sdemo1.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/google")
    public ResponseEntity<OAuth2UserInfo> googleLogin() {
        return ResponseEntity.ok(oAuth2Service.getGoogleUserInfo());
    }
} 