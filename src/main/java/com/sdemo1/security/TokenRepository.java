package com.sdemo1.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class TokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String username, String refreshToken, long validityInSeconds) {
        String key = "refresh_token:" + username;
        redisTemplate.opsForValue().set(key, refreshToken, validityInSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String username) {
        String key = "refresh_token:" + username;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String username) {
        String key = "refresh_token:" + username;
        redisTemplate.delete(key);
    }

    public void addToBlacklist(String token, long validityInSeconds) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "blacklisted", validityInSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        String key = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
} 