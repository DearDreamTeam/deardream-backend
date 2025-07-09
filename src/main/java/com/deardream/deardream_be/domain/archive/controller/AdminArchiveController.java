package com.deardream.deardream_be.domain.archive.controller;

import com.deardream.deardream_be.domain.archive.dto.AdminArchive;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.service.ArchiveService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminArchiveController {

    private final ArchiveService archiveService;

    @GetMapping("/archives")
    public ApiResponse<AdminArchive> getAllArchives(
            @RequestParam int year,
            @RequestParam int month
    ) {
        AdminArchive adminArchive = archiveService.getDeliveriesByYearAndMonth(year, month);
        return ApiResponse.onSuccess(adminArchive);
    }

    // HOME delivery status 변경
    @PostMapping("/{archiveId}/home")
    public ApiResponse<?> updateHomeDeliveryStatus(
            @PathVariable Long archiveId,
            @RequestParam DeliveryStatus deliveryStatus
            ) {
        archiveService.updateHomeDeliverStatus(archiveId, deliveryStatus);
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    // INSTITUTION delivery status 변경
    @PostMapping("/{institutionId}/institution")
    public ApiResponse<?> updateInstitutionDeliveryStatus(
            @PathVariable Long institutionId,
            @RequestParam DeliveryStatus deliveryStatus,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        archiveService.updateInstitutionDeliveryStatus(institutionId, deliveryStatus, year, month);
        return ApiResponse.onSuccess(SuccessStatus._OK);

    }
}

