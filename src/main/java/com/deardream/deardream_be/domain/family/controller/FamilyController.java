package com.deardream.deardream_be.domain.family.controller;

import com.deardream.deardream_be.domain.family.dto.FamilyInvitationDto;
import com.deardream.deardream_be.domain.family.dto.FamilyMembersResponseDto;
import com.deardream.deardream_be.domain.family.dto.FamilyResponseDto;
import com.deardream.deardream_be.domain.family.service.FamilyService;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "가족 그룹을 생성합니다. 대표자만 가능합니다.")
    @PostMapping
    public ApiResponse<FamilyResponseDto> createMyFamily(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        FamilyResponseDto familyResponseDto = familyService.createMyFamily(kakaoId);
        return ApiResponse.onSuccess(familyResponseDto);
    }

    // 2) 나의 가족 조회
    @Operation(summary = "가족의 전체 구성원 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<FamilyMembersResponseDto> getMyFamily(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        FamilyMembersResponseDto familyMembersResponseDto = familyService.getMyFamily(kakaoId);
        return ApiResponse.onSuccess(familyMembersResponseDto);
    }

    // 3) 가족 초대장 정보 조회 (토큰 없음) - 추가
    @Operation(summary = "로그인 전 가족 초대장 정보를 조회합니다.")
    @GetMapping("/invitation")
    public ApiResponse<FamilyInvitationDto> getMyInvitation(
            @RequestParam("code") String inviteCode
    ) {
        FamilyInvitationDto familyInvitation = familyService.getMyFamilyInvitation(inviteCode);
        return ApiResponse.onSuccess(familyInvitation);
    }


    // 4) 초대 링크 생성 (LEADER)
    @Operation(summary = "가족 그룹 초대 링크를 생성합니다. 대표자만 가능합니다.")
    @PostMapping("/link")
    public ApiResponse<String> createInviteLink(
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        String link = familyService.createInviteLink(userId);
        return ApiResponse.onSuccess(link);
    }


    // 5) 초대 링크 조회 - 추가
    @Operation(summary = "가족 그룹 초대 링크를 조회합니다.")
    @GetMapping("/link")
    public ApiResponse<String> getInviteLink(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();
        String link = familyService.getInviteLink(kakaoId);

        return ApiResponse.onSuccess(link);
    }

    // 5) 초대 링크로 가입 (role=USER)
    @Operation(summary = "사전에 회원 가입이 완료된 가입자가 가족 초대 링크로 들어올 시 가족 멤버로 추가합니다.")
    @PostMapping("/join")
    public ApiResponse<Void> joinByInviteCode(
            @RequestParam("code") String inviteCode,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long kakaoId = userDetails.getKakaoId();

        familyService.joinByInviteCode(inviteCode, kakaoId);
        return ApiResponse.onSuccess(null);
    }
}