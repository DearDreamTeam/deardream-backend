package com.deardream.deardream_be.domain.recipient.dto;

import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipientRequestDto {
    private Long familyId;
    private Long leaderId;
    private String name;
    private String birth;
    private String calendarType;
    private String phone;
    private DeliveryType deliveryType;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Long code; // Institution의 PK
}
