package com.deardream.deardream_be.domain.cookie;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

public class CookieUtil {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String TEMP_TOKEN = "tempToken";

    // 필요하다면 SameSite 값을 "Strict"나 "Lax", "None" 등으로 변경
    private static final String SAME_SITE = "Strict";
    private static final String PATH = "/";

    // 액세스 토큰 세팅
    public static ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_TOKEN, token)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(SAME_SITE)
                .build();
    }

    // 리프레시 토큰 세팅
    public static ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN, token)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(SAME_SITE)
                .build();
    }

    public static ResponseCookie createTempTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(TEMP_TOKEN, token)
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(SAME_SITE)
                .build();
    }

    // 액세스 토큰 쿠키 삭제 (로그아웃 등)
    public static ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build();
    }

    // 리프레시 토큰 쿠키 삭제
    public static ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build();
    }

    public static ResponseCookie deleteTempTokenCookie() {
        return ResponseCookie.from(TEMP_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .path(PATH)
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build();
    }
}
