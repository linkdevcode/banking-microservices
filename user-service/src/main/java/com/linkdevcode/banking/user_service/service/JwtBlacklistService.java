package com.linkdevcode.banking.user_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtBlacklistService {

    private static final String BLACKLIST_PREFIX = "JWT_BLACKLIST:";
    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Adds a JWT token to the Redis Blacklist to invalidate it immediately.
     * The token's TTL is set to its remaining validity time.
     * @param token The JWT string to be blacklisted.
     * @param expirationSeconds The remaining time (in seconds) until the token naturally expires.
     */
    public void blacklistToken(String token, long expirationSeconds) {
        // Use a unique ID or the token itself as the key.
        // Storing the token itself is fine for blacklisting purposes.
        String key = BLACKLIST_PREFIX + token;
        // The value doesn't matter much; we use 'invalidated'
        redisTemplate.opsForValue().set(key, "invalidated", expirationSeconds, TimeUnit.SECONDS);
    }

    /**
     * Checks if a token is present in the Redis Blacklist.
     * @param token The JWT string to check.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }
}