package com.deardream.deardream_be.domain.payment.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.payment.Payment;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.payment.dto.PlanResponseDto;
import com.deardream.deardream_be.domain.payment.dto.SubscriptionDto;
import com.deardream.deardream_be.domain.payment.exception.PaymentErrorCode;
import com.deardream.deardream_be.domain.payment.exception.PaymentException;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.BaseCode;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;

    @Transactional
    public List<SubscriptionDto> getSubscriptions(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        List<Payment> payments = paymentRepository.findAllByFamilyId(family.getId());

        // 결제는 승인 된 것만 필터링하여 SubscriptionDto로 변환
        return payments.stream()
                .filter(payment -> payment.getApprovedAt() != null)
                .map(payment -> SubscriptionDto.builder()
                .paymentDate(payment.getApprovedAt())
                .amount(8900)
                .build()).collect(Collectors.toList());

    }


    public PlanResponseDto getPlanStatus(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        Recipient recipient = family.getRecipient();

        if(recipient == null) {
           throw new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND);
        }

        DeliveryType status = recipient.getDeliveryType();
        return PlanResponseDto.builder()
                .isActive(family.getIsActive())
                .type(status)
                .build();


    }

    @Transactional
    public void rejoinByInstitution(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        if (family.getIsActive()) {
            throw new GeneralException(ErrorStatus._SUBSCRIPTION_IS_ALREADY_ACTIVE);
        }

        family.setFamilyActive();
    }

    @Transactional
    public void deActiveInstitution(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        Family family = familyRepository.findByLeaderId(user.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        family.setFamilyDeActive();
    }
}
