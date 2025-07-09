package com.deardream.deardream_be.domain.family.controller;

import com.deardream.deardream_be.domain.family.dto.FamilyMembersResponseDto;
import com.deardream.deardream_be.domain.family.dto.FamilyResponseDto;
import com.deardream.deardream_be.domain.family.service.FamilyService;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    // 1) 가족 생성 (role=LEADER)
    @PostMapping
    public ApiResponse<FamilyResponseDto> createFamily(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        FamilyResponseDto familyResponseDto = familyService.createFamily(kakaoId);
        return ApiResponse.onSuccess(familyResponseDto);
    }

    // 2) 나의 가족 조회
    @GetMapping
    public ApiResponse<FamilyMembersResponseDto> getMyFamily(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        FamilyMembersResponseDto familyMembersResponseDto = familyService.getMyFamily(kakaoId);
        return ApiResponse.onSuccess(familyMembersResponseDto);
    }

    // 3) 초대 링크 생성 (LEADER)
    @GetMapping("/link")
    public ApiResponse<String> getInviteLink(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        String link = familyService.createInviteLink(kakaoId);
        return ApiResponse.onSuccess(link);
    }

    // 4) 초대 링크로 가입 (role=USER)
    @PostMapping("/join")
    public ApiResponse<Void> joinByInvite(
            @RequestParam("code") String inviteCode,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        familyService.joinByInvite(inviteCode, kakaoId);
        return ApiResponse.onSuccess(null);
    }
}
