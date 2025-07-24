package com.deardream.deardream_be.domain.payment.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.payment.SubscriptionRepository;
import com.deardream.deardream_be.domain.payment.entity.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.SubscriptionDto;
import com.deardream.deardream_be.domain.payment.entity.Subscription;
import com.deardream.deardream_be.domain.payment.entity.SubscriptionStatus;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KakaoPayService kakaoPayService;
    private final FamilyRepository familyRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void subscribe() {
        LocalDateTime today = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<Subscription> subscriptions = subscriptionRepository.findAllByStatus(SubscriptionStatus.ACTIVE);

        for(Subscription subscription : subscriptions) {
            // 만료된 구독인지 확인
            if(subscription.getExpiredAt().isBefore(today)) {
                // 구독 만료인 경우 정기 결제 갱신
                kakaoPayService.createPaymentForSubscription(subscription);
            }
        }
    }

    @Transactional
    public List<SubscriptionDto> getPayment(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        List<Payment> payments = paymentRepository.findAllByUser(family.getLeader());


        return payments.stream()
                .map(payment -> {
                    return SubscriptionDto.builder()
                            .paymentDate(payment.getApprovedAt()) // 결제 승인일
                            .amount(getAmountBasedOnDeliveryType(payment)) // 구독 금액 (구독 유형에 따라 다름)
                            .build();
                })
                .collect(Collectors.toList());

    }

    private int getAmountBasedOnDeliveryType(Payment payment) {
        if (payment.getDeliveryType() == DeliveryType.HOME) {
            return 6900; // 가정배송의 경우 월 6900원
        } else if (payment.getDeliveryType() == DeliveryType.INSTITUTION) {
            return 39000; // 기관방문의 경우 월 39000원
        } else {
            throw new GeneralException(ErrorStatus._INVALID_DELIVERY_TYPE);
        }
    }


}
