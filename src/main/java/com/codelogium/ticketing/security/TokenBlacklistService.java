package com.codelogium.ticketing.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /** 将 Token 加入黑名单，TTL = 该 Token 的剩余有效时间 */
    public void banToken(String token) {
        try {
            DecodedJWT jwt = jwtUtil.verifyToken(token);
            Date expiresAt = jwt.getExpiresAt();
            long ttlSeconds = Duration.between(Instant.now(), expiresAt.toInstant()).getSeconds();
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "logout",
                        Duration.ofSeconds(ttlSeconds));
            }
        } catch (Exception e) {
            log.warn("Redis 不可用，登出操作降级: {}", e.getMessage());
        }
    }

    /** 检查 Token 是否已被拉黑。Redis 不可用时降级放行（false = 不禁用） */
    public boolean isTokenBanned(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.warn("Redis 不可用，黑名单检查降级放行: {}", e.getMessage());
            return false;
        }
    }
}
