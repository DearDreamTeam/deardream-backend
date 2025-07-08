package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRequestDto {
    private DeliveryStatus deliveryStatus;
    private Integer year;
    private Integer month;
}
