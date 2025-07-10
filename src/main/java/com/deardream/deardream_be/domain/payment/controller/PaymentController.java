package com.deardream.deardream_be.domain.payment.controller;

import com.deardream.deardream_be.domain.payment.dto.SubscriptionDto;
import com.deardream.deardream_be.domain.payment.service.PaymentService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
