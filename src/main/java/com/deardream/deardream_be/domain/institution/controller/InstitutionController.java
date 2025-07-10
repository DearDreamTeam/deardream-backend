package com.deardream.deardream_be.domain.institution.controller;

import com.deardream.deardream_be.domain.institution.dto.CreateInstitutionDto;
import com.deardream.deardream_be.domain.institution.dto.InstitutionResponseDto;
import com.deardream.deardream_be.domain.institution.service.InstitutionService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    @Operation(summary = "[어드민 기능]기관 생성 API, 기관 코드를 return 합니다.")
    @PostMapping("/admin")
    public ApiResponse<String> createInstitution(
            @RequestBody CreateInstitutionDto request
            ) {
        return ApiResponse.onSuccess(institutionService.generateInstitutionCode(request));
    }

    @Operation(summary = "기관 정보 요청 API")
    @GetMapping
    public ApiResponse<InstitutionResponseDto> checkInstitution(
            @RequestParam String code
    ) {
        return ApiResponse.onSuccess(institutionService.getInstitutionInfo(code));
    }

    @Operation(summary = "[어드민 기능] 모든 기관 정보 요청 API")
    @GetMapping("/admin")
    public ApiResponse<List<InstitutionResponseDto>> getAllInstitutions() {
        return ApiResponse.onSuccess(institutionService.getAllInstitutionInfo());
    }

}
