package com.deardream.deardream_be.domain.payment.dto;

import com.deardream.deardream_be.domain.payment.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionDto {
    private LocalDateTime paymentDate; // 결제 날짜
    private int amount; // 결제 금액
}
