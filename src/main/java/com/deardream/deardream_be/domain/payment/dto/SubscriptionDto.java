package com.deardream.deardream_be.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SubscriptionDto {
    private LocalDate paymentDate; // 결제 날짜
    private int amount; // 결제 금액
}
