package com.deardream.deardream_be.domain.auth.service;

import com.deardream.deardream_be.domain.auth.dto.KakaoDto;
import com.deardream.deardream_be.domain.auth.util.KakaoUtil;
//import com.deardream.deardream_be.domain.jwt.util.JwtUtil;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImplementation implements AuthService {

    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User loginWithKakao(String code) {
        // 1. 카카오에서 access token 요청
        KakaoDto.OAuthToken tokenResponse = kakaoUtil.getAccessToken(code);

        // 2. 카카오에서 사용자 정보 요청
        KakaoDto.KakaoProfile kakaoProfile = kakaoUtil.getUserInfo(tokenResponse.getAccess_token());
        Long kakaoId = kakaoProfile.getId();
        String email = kakaoProfile.getKakao_account().getEmail();
        String profileImage = kakaoProfile.getKakao_account().getProfile().getProfile_image_url();
        String name = kakaoProfile.getKakao_account().getName() != null
                ? kakaoProfile.getKakao_account().getName()
                : kakaoProfile.getProperties().getNickname();


        // 3. 이메일을 통해 기존 유저 확인 또는 신규 유저 등록
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoId)
                                .name(name)
                                .profileImage(profileImage)
                                .email(email)
                                .build()
                ));
    }

    @Override
    public String generateAccessToken(User user) { return jwtUtil.createAccessToken(user.getKakaoId());}

    @Override
    public String generateRefreshToken(User user) {
        return jwtUtil.createRefreshToken(user.getKakaoId());
    }
}
