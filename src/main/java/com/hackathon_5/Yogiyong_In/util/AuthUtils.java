package com.hackathon_5.Yogiyong_In.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class AuthUtils {

    private AuthUtils(){}

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
}
