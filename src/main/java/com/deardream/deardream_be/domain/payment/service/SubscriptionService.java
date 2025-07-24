package com.deardream.deardream_be.domain.payment.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.SubscriptionRepository;
import com.deardream.deardream_be.domain.payment.entity.Payment;
import com.deardream.deardream_be.domain.payment.entity.Subscription;
import com.deardream.deardream_be.domain.payment.entity.SubscriptionStatus;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubscriptionService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;

    // 구독 갱신 상태
    @Transactional
    public void updateSubscriptionStatus(Payment payment) {
        Subscription subscription = payment.getSubscription();

        if (subscription == null) {
            // 첫 결제 후 구독 활성화
            subscription = new Subscription(payment, payment.getUser().getFamily());
            subscriptionRepository.save(subscription);
        } else {
            subscription.extend(payment);
        }
    }

    // 구독 취소
    @Transactional
    public void cancelSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        Family family = familyRepository.findByLeaderId(user.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findByFamily(family);

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.cancel();
            subscriptionRepository.save(subscription);

            Payment payment = subscription.getPayment();

            if (payment != null && payment.getSid() != null) {
                // 결제 취소
                payment.cancel();
                paymentRepository.save(payment);
            } else {
                throw new PaymentException(PaymentErrorCode._SUBSCRIPTION_NOT_FOUND);
            }

        }
    }
}


