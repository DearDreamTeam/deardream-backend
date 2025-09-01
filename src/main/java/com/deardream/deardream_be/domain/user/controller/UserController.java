package com.deardream.deardream_be.domain.user.controller;

import com.deardream.deardream_be.domain.auth.service.AuthService;
import com.deardream.deardream_be.domain.cookie.CookieService;
import com.deardream.deardream_be.domain.cookie.CookieUtil;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import com.deardream.deardream_be.domain.user.dto.RegisterResponseDto;
import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.service.UserService;
import com.deardream.deardream_be.domain.user.service.UserServiceWithdraw;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserServiceWithdraw userServiceWithdraw;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;


    /**
     * @param authorization 인증 객체
     * @param userRequestDto 등록할 사용자 정보
     * @return 등록된 사용자 정보
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RegisterResponseDto> registerUser(
            @RequestParam(value = "code", required = false) String inviteCode,
            @RequestHeader("Authorization") String authorization,
            @RequestPart("userRequestDto") @Valid UserRequestDto userRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletResponse response

    ) {

        // 1. 헤더 유효성 검사
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        // 2. 임시 토큰에서 kakaoId 파싱
        String tempToken = authorization.substring(7);
        Long kakaoId = jwtUtil.getKakaoId(tempToken);

        // 3. 프로필 등록
        RegisterResponseDto registerResponseDto = userService.register(kakaoId, userRequestDto, profileImage, inviteCode);

        // 4. 쿠키 등록
        cookieService.setTokenCookies(response, registerResponseDto.getAccessToken(), registerResponseDto.getRefreshToken());

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
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserResponseDto> updateMyInfo(
            Authentication authentication,
            @RequestPart("userRequestDto") @Valid UserRequestDto userRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        UserResponseDto userInfo = userService.updateMyInfo(kakaoId, userRequestDto, profileImage);
        return ApiResponse.onSuccess(userInfo);
    }

//    /**
//     * 내 계정 삭제 - 사용 안함
//     */
//    @DeleteMapping("/me")
//    public ApiResponse<Void> deleteMyAccount(
//            Authentication authentication
//    ) {
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        Long kakaoId = userDetails.getKakaoId();
//        userService.deleteMyAccount(kakaoId);
//        return ApiResponse.of(SuccessStatus._OK, null);
//
//    }

    /**
     * 현재 로그인된 사용자의 회원 탈퇴 처리
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(
            Authentication authentication,
            @RequestHeader("Authorization") String token,
            HttpServletResponse response
    ) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        // Bearer 접두사 제거
        String accessToken = token.replace("Bearer ", "");

        // redis에서 리프레시 토큰 삭제 -> 접근 불가하게 만듦
        authService.logout(accessToken);

        userServiceWithdraw.withdraw(kakaoId);

        cookieService.deleteTokenCookies(response);
        cookieService.deleteTempTokenCookie(response);

        return ApiResponse.of(SuccessStatus._OK, null);
    }
}