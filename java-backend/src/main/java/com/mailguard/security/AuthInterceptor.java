package com.mailguard.security;

import com.mailguard.exception.ApiException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTR = "userId";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS preflight la token nasto
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing or invalid Authorization header. Please login first.");
        }
        try {
            Long userId = jwtUtil.parseUserId(header.substring(7).trim());
            request.setAttribute(USER_ID_ATTR, userId);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            throw ApiException.unauthorized("Invalid or expired token. Please login again.");
        }
    }
}
