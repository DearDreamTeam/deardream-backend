package com.deardream.deardream_be.domain.user.controller;

import com.deardream.deardream_be.domain.user.dto.UserRequestDto;
import com.deardream.deardream_be.domain.user.dto.UserResponseDto;
import com.deardream.deardream_be.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원 정보 등록 (토큰에서 kakaoId 추출)
     *
     * @param authentication 인증 객체
     * @param userRequestDto 등록할 사용자 정보
     * @return 등록된 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(
            Authentication authentication,
            @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        log.info("[UserController] authentication: {}", authentication);
        Long kakaoId = Long.valueOf(authentication.getPrincipal().toString());
        UserResponseDto userInfo = userService.register(kakaoId, userRequestDto);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 내 정보 조회
     *
     * @return 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyInfo(
            Authentication authentication
    ) {
        Long kakaoId = Long.valueOf(authentication.getPrincipal().toString());
        UserResponseDto userInfo = userService.getMyInfo(kakaoId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 내 정보 수정
     *
     * @param userRequestDto 수정할 정보
     * @return 수정된 사용자 정보
     */
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            Authentication authentication,
            @RequestBody @Valid UserRequestDto userRequestDto
    ) {
        Long kakaoId = Long.valueOf(authentication.getPrincipal().toString());
        UserResponseDto userInfo = userService.updateMyInfo(kakaoId, userRequestDto);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 내 계정 삭제
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(
            Authentication authentication
    ) {
        Long kakaoId = Long.valueOf(authentication.getPrincipal().toString());
        userService.deleteMyAccount(kakaoId);
        return ResponseEntity.ok(
                Map.of(
                        "isSuccess", true,
                        "code", 200,
                        "message", "회원 탈퇴가 정상적으로 처리되었습니다."
                )
        );

    }
}