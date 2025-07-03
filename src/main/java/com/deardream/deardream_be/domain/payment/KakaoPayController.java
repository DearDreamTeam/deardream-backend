package com.deardream.deardream_be.domain.payment;

import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.KakaoReadyResponse;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.payment.service.KakaoPayService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.LifecycleState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    // 결제 요청
    @PostMapping("/ready")
    public ApiResponse<KakaoReadyResponse> readyToKakaoPay(
            @RequestParam Long familyId
    ) {
        return ApiResponse.onSuccess(kakaoPayService.kakaoPayReady(familyId));
    }


    @GetMapping("/success")
    public ApiResponse<KakaoApproveResponse> afterPayRequest(
            @RequestParam String pgToken,
            @RequestParam String tid
    ) {
        KakaoApproveResponse response = kakaoPayService.approveResponse(tid, pgToken);

        return ApiResponse.onSuccess(response);
    }

    // 결제 진행 중 취소
    @GetMapping("/cancel")
    public void canclePay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_CANCELLED);
    }

    // 결제 실패
    @GetMapping("/fail")
    public void failPay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_APPROVE_FAILED);
    }

    // 정기 결제 비활성화
    @PostMapping("/inactive")
    public void inactivePay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_CANCELLED);

    }

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 기준 결제
    public void subscriptionPay() {
        throw new PaymentException(PaymentErrorCode._PAYMENT_ALREADY_COMPLETED);
    }
}
