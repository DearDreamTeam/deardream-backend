package com.deardream.deardream_be.domain.payment.controller;

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

    @Operation(summary = "플랜 서비스를 해제합니다. 결제한 사람의 아이디를 넣어주세요.")
    @PatchMapping("/cancel")
    public ApiResponse<Void> cancelPlan(
            @RequestParam Long userId
    ) {
        return ApiResponse.onSuccess(paymentService.deActive(userId));
    }
}
