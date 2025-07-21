package com.deardream.deardream_be.domain.archive.controller;

import com.deardream.deardream_be.domain.archive.dto.*;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.service.ArchiveService;
import com.deardream.deardream_be.domain.institution.dto.DeleteUsersRequestDto;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/master/admin")
public class AdminArchiveController {

    private final ArchiveService archiveService;

    @Operation(summary = "[마스터 어드민 기능] 월별 소식지를 조회합니다.")
    @GetMapping("/archives")
    public ApiResponse<AdminArchive> getAllArchives(
            @RequestParam int year,
            @RequestParam int month
    ) {
        AdminArchive adminArchive = archiveService.getDeliveriesByYearAndMonth(year, month);
        return ApiResponse.onSuccess(adminArchive);
    }

    @Operation(summary = "[마스터 어드민 기능] 개인 플랜에 대한 회원 정보와 pdf를 조회합니다.")
    @GetMapping("/home/archives")
    public ApiResponse<AdminHomeArchiveResponse> getAllArchivesByHome(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.onSuccess(archiveService.getHomeArchives(year, month));
    }

    @Operation(summary = "[마스터 어드민 기능] 기관 플랜에 대한 회원 정보와 pdf를 조회합니다.")
    @GetMapping("/institution/{institutionId}/archives")
    public ApiResponse<AdminInstitutionArchiveResponse> getAllArchivesByInstitution(
        @PathVariable Long institutionId,
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ApiResponse.onSuccess(archiveService.getInstitutionArchives(institutionId, year, month));
    }


    // HOME delivery status 변경
    @Operation(summary = "[마스터 어드민 기능] 가정 배송에 대한 배송 상태를 변경합니다.")
    @PostMapping("/{archiveId}/home")
    public ApiResponse<?> updateHomeDeliveryStatus(
            @PathVariable Long archiveId,
            @RequestParam DeliveryStatus deliveryStatus
            ) {
        archiveService.updateHomeDeliverStatus(archiveId, deliveryStatus);
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    // INSTITUTION delivery status 변경
    @Operation(summary = "[마스터 어드민 기능] 기관 배송에 대환 일괄 배송 상태를 변경합니다.")
    @PostMapping("/{institutionId}/institution")
    public ApiResponse<?> updateInstitutionDeliveryStatus(
            @PathVariable Long institutionId,
            @RequestBody AdminRequestDto request
            ) {
        archiveService.updateInstitutionDeliveryStatus(institutionId, request);
        return ApiResponse.onSuccess(SuccessStatus._OK);

    }



}

