package com.deardream.deardream_be.domain.payment.dto;

import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlanResponseDto {
    private Boolean isActive;
    private DeliveryType type;
}
