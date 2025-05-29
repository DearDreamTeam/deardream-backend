package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.user.entity.User;

public interface AuthService {
    User loginWithKakao(String code); // OAuth 로그인 후 사용자 정보 등록 or 조회
    String generateAccessToken(User user); // JWT 발급
    String generateRefreshToken(User user); // Refresh 토큰 발급
}
