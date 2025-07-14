package com.deardream.deardream_be.domain.payment.controller;

import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final PaymentRepository paymentRepository;
    private final KakaoPayService kakaoPayService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void monthlySubscriptions() {
        LocalDate cutOff = LocalDate.now().minusMonths(1);

        List<Payment> expiredPayments = paymentRepository.findExpiredActivePayments(cutOff);

        for(Payment payment : expiredPayments) {
            try {
                Long userId = payment.getUser().getId();

                log.info("Cancelling subscription for user: {}, payment ID: {}", userId, payment.getTid());

                KakaoApproveResponse response = kakaoPayService.subscriptionPayment(userId);

                log.info("Subscription cancelled successfully for user: {}, payment ID: {}", userId, payment.getTid());
            } catch (Exception e) {
                log.error("정기 결제 실패: userId = " + payment.getUser().getId());
                payment.updateCancel();
                paymentRepository.save(payment);
            }
        }
    }

}
