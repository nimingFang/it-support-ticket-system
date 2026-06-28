package com.codelogium.ticketing.security;

public class SecurityConstants {
    // SECRET_KEY 和 TOKEN_EXPIRATION 已迁移至 application.properties，由 JwtUtil 管理
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION = "Authorization";
    public static final String REGISTER_PATH = "/users";
}
