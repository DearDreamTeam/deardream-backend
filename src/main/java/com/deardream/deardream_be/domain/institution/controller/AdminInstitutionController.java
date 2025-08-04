package com.deardream.deardream_be.domain.institution.controller;

import com.deardream.deardream_be.domain.archive.service.ArchiveService;
import com.deardream.deardream_be.domain.institution.dto.DeleteUsersRequestDto;
import com.deardream.deardream_be.domain.institution.service.AdminInstitutionService;
import com.deardream.deardream_be.domain.institution.service.InstitutionService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/institutions")
public class AdminInstitutionController {

    private final AdminInstitutionService institutionService;

    /*
    @Operation(summary = "[기관 어드민 기능] 사용자 추방이 가능합니다.")
    @DeleteMapping("/institution")
    public ApiResponse<?> deleteUserFromInstitution(
            @RequestBody DeleteUsersRequestDto request
    ) {
        institutionService.deleteUserFromInstitution(request);
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

     */

    @Operation(summary = "[기관 어드민 기능] 사용자에 대한 정보를 조회합니다.")
    @GetMapping("/institution/users")
    public ApiResponse<?> getUsersByInstitution(
            @RequestParam String code
    ) {
        return ApiResponse.onSuccess(institutionService.getUsersByInstitution(code));
    }
}
