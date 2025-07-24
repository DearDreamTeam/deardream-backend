package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    KakaoLoginResponseDto loginWithKakao(String code, Long familyId, HttpServletRequest request); // OAuth 로그인 후 사용자 정보 등록 or 조회
    void logout(String accessToken);
    String logoutWithKakaoAccount(HttpServletRequest request);
}
