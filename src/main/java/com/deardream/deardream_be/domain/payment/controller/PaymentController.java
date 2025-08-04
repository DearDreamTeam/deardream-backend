package com.deardream.deardream_be.domain.payment.controller;

import com.deardream.deardream_be.domain.payment.dto.PlanResponseDto;
import com.deardream.deardream_be.domain.payment.dto.SubscriptionDto;
import com.deardream.deardream_be.domain.payment.service.PaymentService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test/payment/request")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "가정 배송의 결제 내역을 조회합니다.")
    @GetMapping
    public ApiResponse<List<SubscriptionDto>> getAllPayments(
            @RequestParam Long familyId
    ) {
        return ApiResponse.onSuccess(paymentService.getSubscriptions(familyId));
    }


    @Operation(summary = "기관 플랜 서비스를 해제합니다. 리더 아이디를 넣어주세요.")
    @PatchMapping("/cancel/institution")
    public ApiResponse<?> cancelInsitution(
            @RequestParam Long userId
    ) {
        paymentService.deActiveInstitution(userId);
        return ApiResponse.onSuccess("기관 플랜 서비스가 해제되었습니다.");
    }

    @Operation(summary = "현재 플랜 상태를 나타냅니다.")
    @GetMapping("/status/{familyId}")
    public ApiResponse<?> getPlanStatus(
            @PathVariable Long familyId
    ) {
        PlanResponseDto isActive =  paymentService.getPlanStatus(familyId);
        return ApiResponse.onSuccess(
                isActive
        );
    }

    @Operation(summary = "기관 플랜 해지 후 재가입")
    @PostMapping("/rejoin")
    public ApiResponse<?> rejoinPlan(
            @RequestParam Long familyId
    ) {
        paymentService.rejoinByInstitution(familyId);
        return ApiResponse.onSuccess("플랜이 재가입되었습니다.");
    }
}
