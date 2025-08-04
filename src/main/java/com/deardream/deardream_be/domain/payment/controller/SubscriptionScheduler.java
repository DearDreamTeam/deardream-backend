package com.deardream.deardream_be.domain.payment.controller;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.PaymentStatus;
import com.deardream.deardream_be.domain.payment.dto.KakaoApproveResponse;
import com.deardream.deardream_be.domain.payment.dto.request.KakaoCancelSubscription;
import com.deardream.deardream_be.domain.payment.service.KakaoPayService;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final PaymentRepository paymentRepository;
    private final FamilyRepository familyRepository;
    private final KakaoPayService kakaoPayService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void monthlySubscriptions() {
        List<Payment> activePayment = paymentRepository.findAllByStatusAndSidIsNotNull(PaymentStatus.ACTIVE);

        for(Payment payment : activePayment) {
            try {
                if(!isSubscription(payment)) {
                    log.info("현재 정기 결제 시점이 아닙니다. paymentId={}, familyId={}", payment.getId(), payment.getFamilyId());
                    continue;
                }

                Family family = familyRepository.findById(payment.getFamilyId())
                        .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

                if(!family.getIsActive()){
                    log.info("가족이 활성화되지 않았습니다. paymentId={}, familyId={}", payment.getId(), payment.getFamilyId());
                    continue;
                }

                kakaoPayService.subscriptionPayment(payment.getSid(), family.getId());

            } catch (Exception e) {
                log.error("정기 결제 실패: paymentId = " + payment.getId() + ", familyId = " + payment.getFamilyId(), e);

                Family family = familyRepository.findById(payment.getFamilyId())
                        .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

                KakaoCancelSubscription subscription = new KakaoCancelSubscription().build(
                        family.getId(),
                        family.getLeader().getId()
                );

                // 정기 구독 중 오류 시 정기 구독이 해제됩니다.
                kakaoPayService.inactiveSubscription(subscription);

            }
        }

    }

    private boolean isSubscription(Payment payment) {
        if(payment.getApprovedAt() == null)
            return false;

        return !LocalDateTime.now(ZoneId.of("Asia/Seoul")).isBefore(payment.getExpiredAt());
    }

}
