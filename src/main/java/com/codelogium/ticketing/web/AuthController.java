package com.codelogium.ticketing.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codelogium.ticketing.common.Result;
import com.codelogium.ticketing.security.SecurityConstants;
import com.codelogium.ticketing.security.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final TokenBlacklistService blacklistService;

    @PostMapping("/user/logout")
    public Result<?> logout(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (header != null && header.startsWith(SecurityConstants.BEARER)) {
            String token = header.replace(SecurityConstants.BEARER, "");
            blacklistService.banToken(token);
        }
        return Result.success("已登出");
    }
}
