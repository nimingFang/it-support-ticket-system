package com.codelogium.ticketing.security;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public Algorithm getAlgorithm() {
        return Algorithm.HMAC512(secret);
    }

    public String createToken(String username, List<String> authorities) {
        return JWT.create()
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(getAlgorithm());
    }

    public DecodedJWT verifyToken(String token) {
        return JWT.require(getAlgorithm()).build().verify(token);
    }
}
