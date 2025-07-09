package com.deardream.deardream_be.domain.recipient.controller;

import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.service.RecipientService;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.service.UserService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RecipientController {

    private final RecipientService recipientService;
    private final UserService userService;

    // get : 수신자 정보 보기 (모든 인증 사용자)
    @GetMapping("/recipient")
    public ResponseEntity<RecipientResponseDto> getRecipient(Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 2. userId로 familyId 조회
        Long familyId = userService.getFamilyIdByUserId(userDetails.getUserId());

        // 3. familyId로 recipient 조회
        RecipientResponseDto
                recipientResponseDto = recipientService.findByFamilyId(familyId);

        // 4. recipient가 없으면 빈 화면(204 No Content 등) // 여기 고쳐야함
        if (recipientResponseDto == null) {
            return ResponseEntity.noContent().build();
        }
        // 5. 있으면 Dto로 변환해 반환
        return ResponseEntity.ok(recipientResponseDto);
    }

    // 수신자의 정보를 post하는 api
    @PostMapping("/recipients")
    public ApiResponse<RecipientResponseDto> createRecipient(
            Authentication authentication,
            @RequestBody @Valid RecipientRequestDto recipientRequestDto
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 1. LEADER 권한 확인
        Role role = userDetails.getRole();
        if(role != Role.LEADER){
            throw new AccessDeniedException("수정할 수 있는 권한이 없습니다. 대표자만 정보 수정이 가능합니다.");
        }

        // 2. 등록 (로그인된 사용자의 id를 leaderId로 넘김)
        RecipientResponseDto response = recipientService.createRecipient(userDetails, recipientRequestDto);
        return ApiResponse.onSuccess(response);

    }

    // PATCH 수신자 주소 및 수령 방식 등록, 수정 (대표자만 가능)
    @PatchMapping("/recipients/{recipientId}")
    public ApiResponse<RecipientAddressUpdateDto> updateRecipientAddress(Authentication authentication, @PathVariable Long recipientId, @RequestBody @Valid RecipientAddressUpdateDto recipientAddressUpdateDto) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Role role = userDetails.getRole();
        if(role != Role.LEADER){
            throw new AccessDeniedException("수정할 수 있는 권한이 없습니다. 대표자만 정보 수정이 가능합니다.");
        }
        RecipientAddressUpdateDto recipientInfo = recipientService.updateRecipientAddress(recipientId, recipientAddressUpdateDto);
        return ApiResponse.onSuccess(recipientInfo);
//
//        // 2. userId로 familyId 조회
//        Long familyId = userService.getFamilyIdByUserId(userDetails.getUserId());
//
//        // 3. familyId로 recipient 조회
//        RecipientResponseDto
//                recipientResponseDto = recipientService.findByFamilyId(familyId);
//
//        // 4. recipientId 꺼내기
//        Long recipientId = recipientResponseDto.getId();


    }


    // PUT 수신자(받는 분) 정보 수정 (대표자만 가능)
    @PutMapping("/recipients/{recipientId}")
    public ApiResponse<RecipientResponseDto> updateRecipientInfo(
            Authentication authentication,
            @PathVariable Long recipientId,
            @RequestBody @Valid RecipientRequestDto recipientRequestDto
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Role role = userDetails.getRole();
        if(role != Role.LEADER){
            throw new AccessDeniedException("수정할 수 있는 권한이 없습니다. 대표자만 정보 수정이 가능합니다.");
        }
        RecipientResponseDto response = recipientService.updateRecipientInfo(recipientId, recipientRequestDto);
        return ApiResponse.onSuccess(response);
    }
}
