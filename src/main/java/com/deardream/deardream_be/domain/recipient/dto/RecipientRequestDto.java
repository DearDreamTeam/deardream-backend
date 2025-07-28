package com.deardream.deardream_be.domain.recipient.dto;

import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RecipientRequestDto {
    private Long familyId;
    private Long leaderId;
    private String name;
    private String profileImage;
    private LocalDate birth;
    private String phone;
    private CalendarType calendarType;
    private RecipientAddressUpdateDto address;
//    private DeliveryType deliveryType;
//    private String address;
//    private String addressDetail;
//    private String postalCode;
//    private String code;
}
