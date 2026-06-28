package com.codelogium.ticketing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.codelogium.ticketing.security.filter.AuthenticationFilter;
import com.codelogium.ticketing.security.filter.ExceptionHandlerFilter;
import com.codelogium.ticketing.security.filter.JWTAuthorizationFilter;
import com.codelogium.ticketing.security.handler.CustomAccessDeniedHandler;
import com.codelogium.ticketing.security.handler.CustomAuthenticationEntryPoint;
import com.codelogium.ticketing.security.manager.CustomAuthenticationManager;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationManager customAuthenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(customAuthenticationManager, jwtUtil);
        authenticationFilter.setFilterProcessesUrl("/user/authenticate");
        // JWTAuthorizationFilter 增加黑名单检查能力
        JWTAuthorizationFilter authorizationFilter = new JWTAuthorizationFilter(jwtUtil, blacklistService);
        http
            .headers(headers -> headers.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.POST, SecurityConstants.REGISTER_PATH).permitAll()
            .requestMatchers("/swagger-ui/*", "/api-docs/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/users/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/users/**").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/users/**").hasAuthority("ADMIN")
            .requestMatchers("/tickets/{ticketId}/info").hasAuthority("EMPLOYEE")
            .anyRequest().authenticated())
            .exceptionHandling(handler -> {
                handler.accessDeniedHandler(new CustomAccessDeniedHandler());
                handler.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
            })
            .addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)
            .addFilter(authenticationFilter)
            .addFilterAfter(authorizationFilter, AuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            return http.build();
    }
}
