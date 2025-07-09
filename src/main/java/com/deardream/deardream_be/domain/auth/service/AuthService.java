package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;

public interface AuthService {
    KakaoLoginResponseDto loginWithKakao(String code, Long familyId); // OAuth 로그인 후 사용자 정보 등록 or 조회
    void logout(String accessToken);
}
