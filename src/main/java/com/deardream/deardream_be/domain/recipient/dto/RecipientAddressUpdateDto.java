package com.deardream.deardream_be.domain.recipient.dto;

import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecipientAddressUpdateDto {
    private DeliveryType deliveryType;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Long code;
}