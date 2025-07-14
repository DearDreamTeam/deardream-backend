package com.deardream.deardream_be.domain.payment.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.SubscriptionDto;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void deActivePayment() {
        LocalDate today = LocalDate.now();

        List<Payment> payments = paymentRepository.findAllByIsActiveTrue();

        for(Payment payment : payments) {
            if(payment.getApprovedAt() != null &&
            payment.getApprovedAt().plusDays(30).isBefore(today)) {
                payment.deActive();
                log.info("구독 해제: {} - {}", payment.getUser().getId(), payment.getTid());
            }
        }

        paymentRepository.saveAll(payments);
    }

    @Transactional
    public List<SubscriptionDto> getSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        List<Payment> payments = paymentRepository.findAllByUser(user);

        return payments.stream().map(payment -> SubscriptionDto.builder()
                .paymentDate(payment.getApprovedAt())
                .amount(8900)
                .build()).collect(Collectors.toList());

    }
}
