package com.deardream.deardream_be.domain.cookie;

import com.deardream.deardream_be.domain.cookie.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public static final long ACCESS_TOKEN_EXPIRES_IN = 60 * 60 * 2;
    public static final long REFRESH_TOKEN_EXPIRES_IN = 60 * 60 * 24 * 2;
    public static final long TEMP_TOKEN_EXPIRES_IN = 60 * 10;

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", CookieUtil.createAccessTokenCookie(token, ACCESS_TOKEN_EXPIRES_IN).toString());
    }


    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", CookieUtil.createRefreshTokenCookie(token, REFRESH_TOKEN_EXPIRES_IN).toString());
    }

    public void setTempTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", CookieUtil.createTempTokenCookie(token, TEMP_TOKEN_EXPIRES_IN).toString());
    }

    // 토큰 삭제(만료)
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", CookieUtil.deleteAccessTokenCookie().toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", CookieUtil.deleteRefreshTokenCookie().toString());
    }

    public void deleteTempTokenCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", CookieUtil.deleteTempTokenCookie().toString());
    }

    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        if (accessToken != null) {
            setAccessTokenCookie(response, accessToken);
        }
        if (refreshToken != null) {
            setRefreshTokenCookie(response, refreshToken);
        }
    }

    public void deleteTokenCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }


}
