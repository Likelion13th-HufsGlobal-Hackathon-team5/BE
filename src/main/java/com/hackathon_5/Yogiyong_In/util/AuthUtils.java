package com.hackathon_5.Yogiyong_In.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public final class AuthUtils {

    private AuthUtils(){}

    /** (기존) 요청에서 액세스 토큰 추출: 쿠키 ACCESS_TOKEN → Authorization: Bearer */
    public static String resolveAccessToken(HttpServletRequest request) {
        // 1) Cookie 우선 (ACCESS_TOKEN)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("ACCESS_TOKEN".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue();
                }
            }
        }
        // 2) Authorization 헤더 (Bearer)
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    /** SecurityContext에서 현재 사용자 ID (없으면 null) */
    public static String currentUserIdOrNull() {
        var ctx = SecurityContextHolder.getContext();
        if (ctx == null || ctx.getAuthentication() == null || !ctx.getAuthentication().isAuthenticated()) {
            return null;
        }
        Object principal = ctx.getAuthentication().getPrincipal();
        if (principal == null) return null;

        if (principal instanceof String s && !"anonymousUser".equals(s)) return s;
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return null;
    }

    /** SecurityContext에서 현재 사용자 ID (없으면 401) */
    public static String currentUserIdOrThrow() {
        String uid = currentUserIdOrNull();
        if (!StringUtils.hasText(uid)) {
            throw new org.springframework.web.server.ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return uid;
    }
}

