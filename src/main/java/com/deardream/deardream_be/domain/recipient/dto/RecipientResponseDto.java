package com.deardream.deardream_be.domain.recipient.dto;

import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecipientResponseDto {
    private Long id;
    private Long familyId;
    private Long leaderId;
    private String name;
    private String birth;
    private String phone;
    private String calendarType;
    private String profileImage;
    private String profileImageKey;
    private RecipientAddressUpdateDto address;
//    private DeliveryType deliveryType;
//    private String address;
//    private String addressDetail;
//    private String postalCode;
//    private String code;
}
