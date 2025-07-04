package com.deardream.deardream_be.domain.recipient.entity;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recipient")
public class Recipient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    @Column(nullable = false)
    private String name;

    @Column
    private String birth;

    @Column(name = "calendar_type")
    private String calendarType;

    @Column
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    private DeliveryType deliveryType;

    @Column
    private String address;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "postal_code")
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code")
    private Institution code;


    // RecipientRequestDto의 필드를 Recipient 엔티티에 적용하여 정보 업데이트
    public void updateWithRecipientRequestDto(
            String name,
            String birth,
            String calendarType,
            String phone,
            DeliveryType deliveryType,
            String address,
            String addressDetail,
            String postalCode,
            Family family,
            User leader,
            Institution code
    ) {
        if (name != null) this.name = name;
        if (birth != null) this.birth = birth;
        if (calendarType != null) this.calendarType = calendarType;
        if (phone != null) this.phone = phone;
        if (deliveryType != null) this.deliveryType = deliveryType;
        if (address != null) this.address = address;
        if (addressDetail != null) this.addressDetail = addressDetail;
        if (postalCode != null) this.postalCode = postalCode;
        if (family != null) this.family = family;
        if (leader != null) this.leader = leader;
        if (code != null) this.code = code;
    }

    public RecipientResponseDto toRecipientResponseDto() {
        return RecipientResponseDto.builder()
                .id(this.id)
                .familyId(this.family != null ? this.family.getId() : null)
                .leaderId(this.leader != null ? this.leader.getId() : null)
                .name(this.name)
                .birth(this.birth)
                .calendarType(this.calendarType)
                .phone(this.phone)
                .deliveryType(this.deliveryType)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .code(this.code != null ? this.code.getCode() : null)
                .build();
    }


    // RecipientRequestAddressDto의 필드를 Recipient 엔티티에 적용하여 정보 업데이트
    public void updateWithRecipientAddressRequestDto(
            DeliveryType deliveryType,
            String address,
            String addressDetail,
            String postalCode,
            Institution code
    ) {
        if (deliveryType != null) this.deliveryType = deliveryType;
        if (address != null) this.address = address;
        if (addressDetail != null) this.addressDetail = addressDetail;
        if (postalCode != null) this.postalCode = postalCode;
        if (code != null) this.code = code;
    }


    public RecipientAddressUpdateDto toRecipientAddressResponseDto() {
        return RecipientAddressUpdateDto.builder()
                .deliveryType(this.deliveryType)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .code(this.code != null ? this.code.getCode() : null)
                .build();
    }
}


