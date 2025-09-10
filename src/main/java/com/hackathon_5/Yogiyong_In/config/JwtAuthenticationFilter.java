package com.hackathon_5.Yogiyong_In.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** ✅ CORS preflight(OPTIONS)는 필터 스킵 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        // 토큰이 없거나 placeholder 값이면 그대로 패스
        if (token == null || token.isBlank()
                || "null".equalsIgnoreCase(token)
                || "undefined".equalsIgnoreCase(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 있고, 아직 인증 컨텍스트가 비어 있을 때만 처리
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtTokenProvider.getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userId = claims.getSubject();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (ExpiredJwtException e) {
                // ⛔️ 여기서 401을 직접 쓰지 않음 — permitAll 경로를 막지 않기 위해 체인 진행
                log.warn("토큰 만료: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (JwtException e) {
                // ⛔️ 유효하지 않은 토큰 — 컨텍스트만 비우고 체인 진행
                log.warn("유효하지 않은 토큰: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // ✅ 인가 여부 판단은 SecurityConfig의 authorizeHttpRequests에서 처리
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1) Authorization: Bearer <token>
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 2) ACCESS_TOKEN 쿠키
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
