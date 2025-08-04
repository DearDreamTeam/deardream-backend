package com.deardream.deardream_be.domain.payment.controller;

import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.KakaoReadyResponse;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoCancelRequest;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoCancelSubscription;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoReadyRequestDto;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.payment.service.KakaoPayService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    // 결제 요청
    @Operation(summary = "카카오페이 결제 요청을 준비합니다. response의 url로 연결해주세요.")
    @PostMapping("/ready")
    public ApiResponse<KakaoReadyResponse> readyToKakaoPay(
            @RequestBody KakaoReadyRequestDto request) {
        return ApiResponse.onSuccess(kakaoPayService.kakaoPayReady(request));
    }


    @Operation(summary = "카카오페이 결제 완료입니다. ready 후 pgToken이 필요합니다.")
    @GetMapping("/success")
    public ApiResponse<KakaoApproveResponse> afterPayRequest(
            @RequestParam String pgToken,
            @RequestParam String tid
    ) {
        KakaoApproveResponse response = kakaoPayService.approveResponse(tid, pgToken);

        return ApiResponse.onSuccess(response);
    }

    // 결제 진행 중 취소
    @Operation(summary = "카카오페이 결제 중 취소 요청입니다.")
    @GetMapping("/cancel")
    public void cancelPay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_CANCELLED);
    }

    // 결제 실패
    @Operation(summary = "카카오페이 결제 실패 요청입니다.")
    @GetMapping("/fail")
    public void failPay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_APPROVE_FAILED);
    }

    @Operation(summary = "[테스트 기능]카카오페이 결제에 대한 취소 기능입니다. 결제 후 3일 이내 취소 가능합니다.")
    @PatchMapping("/refund")
    public ApiResponse<Void> refundPayment(
            @RequestBody KakaoCancelRequest request
            ) {
        kakaoPayService.refundPayment(request);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "카카오페이 정기 결제 비활성화 기능입니다.")
    @PostMapping("/subscription/inactive")
    public ApiResponse<Void> cancelSubscription(
            @RequestBody KakaoCancelSubscription request
    ) {
        kakaoPayService.inactiveSubscription(request);
        return ApiResponse.onSuccess(null);
    }

}
