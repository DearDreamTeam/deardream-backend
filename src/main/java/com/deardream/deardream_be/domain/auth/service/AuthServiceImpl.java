package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoDTO;
import com.deardream.deardream_be.domain.auth.dto.KakaoLoginResponseDTO;
import com.deardream.deardream_be.domain.auth.util.KakaoUtil;
import com.deardream.deardream_be.domain.jwt.util.JwtUtil;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User loginWithKakao(String code) {
        // 1. 카카오에서 access token 요청
        KakaoDTO.OAuthToken tokenResponse = kakaoUtil.getAccessToken(code);

        // 2. 카카오에서 사용자 정보 요청
        KakaoDTO.KakaoProfile userInfo = kakaoUtil.getUserInfo(tokenResponse.getAccessToken());
        String email = userInfo.getEmail();
        String name = userInfo.getName();

        // 3. 기존 유저 확인 또는 신규 유저 등록
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .provider("kakao")
                                .build()
                ));
    }

    @Override
    public String generateAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getEmail());
    }

    @Override
    public String generateRefreshToken(User user) {
        return jwtUtil.createRefreshToken(user.getEmail());
    }
}
