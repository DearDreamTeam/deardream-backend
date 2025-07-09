package com.deardream.deardream_be.domain.user.controller;

import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.dto.RegisterResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.service.UserService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    /**
     * @param authorization 인증 객체
     * @param userRequestDto 등록할 사용자 정보
     * @return 등록된 사용자 정보
     */
    @PostMapping("/register")
    public ApiResponse<RegisterResponseDto> registerUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid UserRequestDto userRequestDto

    ) {

        // 1. 헤더 유효성 검사
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        // 2. 임시 토큰에서 kakaoId 파싱
        String tempToken = authorization.substring(7);
        Long kakaoId = jwtUtil.getKakaoId(tempToken);

        // 3. 프로필 등록
        RegisterResponseDto registerResponseDto = userService.register(kakaoId, userRequestDto);

        return ApiResponse.onSuccess(registerResponseDto);
    }

    /**
     * 내 정보 조회
     *
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        UserResponseDto userInfo = userService.getMyInfo(kakaoId);
        return ApiResponse.onSuccess(userInfo);
    }

    /**
     * 내 정보 수정
     *
     * @param userRequestDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @PatchMapping("/me")
    public ApiResponse<UserResponseDto> updateMyInfo(
            Authentication authentication,
            @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        UserResponseDto userInfo = userService.updateMyInfo(kakaoId, userRequestDto);
        return ApiResponse.onSuccess(userInfo);
    }

    /**
     * 내 계정 삭제
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyAccount(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        userService.deleteMyAccount(kakaoId);
        return ApiResponse.of(SuccessStatus._OK, null);

    }
}