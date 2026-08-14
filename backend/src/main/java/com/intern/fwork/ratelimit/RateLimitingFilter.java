package com.intern.fwork.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    private static final int AUTH_LIMIT = 20;
    private static final long AUTH_WINDOW_MS = 60_000L; // 1 minute

    private static final int API_LIMIT = 300;
    private static final long API_WINDOW_MS = 60_000L; // 1 minute

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow CORS OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for static/actuator endpoints if desired
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey;
        int limit;
        long windowMs;

        if (path.startsWith("/api/auth")) {
            bucketKey = "auth:" + clientIp;
            limit = AUTH_LIMIT;
            windowMs = AUTH_WINDOW_MS;
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                bucketKey = "user:" + auth.getName();
            } else {
                bucketKey = "ip:" + clientIp;
            }
            limit = API_LIMIT;
            windowMs = API_WINDOW_MS;
        }

        if (!rateLimiter.tryConsume(bucketKey, limit, windowMs)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            String json = String.format(
                    "{\"success\":false,\"status\":429,\"message\":\"Too many requests. Please try again later.\",\"localDateTime\":\"%s\"}",
                    LocalDateTime.now()
            );
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
