package com.codelogium.ticketing.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.codelogium.ticketing.security.JwtUtil;
import com.codelogium.ticketing.security.SecurityConstants;
import com.codelogium.ticketing.security.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;
    private TokenBlacklistService blacklistService;

    // 从请求头 Authorization 中提取 Bearer Token
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(SecurityConstants.BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace(SecurityConstants.BEARER, "");
        DecodedJWT decodedJWT = jwtUtil.verifyToken(token);

        // 登出黑名单检查
        if (blacklistService.isTokenBanned(token)) {
            ExceptionHandlerFilter.sendErrorResponse(response,
                    HttpServletResponse.SC_FORBIDDEN, "Token已失效，请重新登录");
            return;
        }

        String username = decodedJWT.getSubject();
        List<String> authorities = decodedJWT.getClaim("authorities").asList(String.class);
        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

}
