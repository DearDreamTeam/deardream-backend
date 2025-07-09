package com.deardream.deardream_be.domain.archive.dto;

import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArchiveResponseDto {
    private String yearMonthType;
    private String pdfUrl;
    private DeliveryStatus deliveryStatus;
}
