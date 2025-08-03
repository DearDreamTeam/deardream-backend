package com.deardream.deardream_be.domain.recipient.entity;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.institution.CalendarType;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    @Column(nullable = false)
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "profile_image_key")
    private String profileImageKey;

    @Column(name = "calendar_type")
    @Enumerated(EnumType.STRING)
    private CalendarType calendarType;

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
    @JoinColumn(name = "code", referencedColumnName = "code")
    private Institution institution;




    // 전체 정보 post 전용
    public RecipientResponseDto toRecipientResponseDto(RecipientAddressUpdateDto dto, String profileImage, String profileImageKey) {
        return RecipientResponseDto.builder()
                .id(this.id)
                .familyId(this.family != null ? this.family.getId() : null)
                .leaderId(this.leader != null ? this.leader.getId() : null)
                .name(this.name)
                .birth(this.birth)
                .profileImage(profileImage != null ? profileImage : this.profileImage)
                .profileImageKey(profileImageKey != null ? profileImageKey : this.profileImageKey)
                .calendarType(this.calendarType)
                .phone(this.phone)
                .address(dto)
                .build();
    }


    // 전체 정보 update 전용
    // RecipientRequestDto의 필드를 Recipient 엔티티에 적용하여 정보 업데이트
    public void updateWithRecipientRequestDto(RecipientRequestDto dto, String profileImage, String profileImageKey) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getBirth() != null) this.birth = dto.getBirth();
        if (dto.getCalendarType() != null) this.calendarType = dto.getCalendarType();
        if (dto.getPhone() != null) this.phone = dto.getPhone();
        if (profileImage != null) this.profileImage = profileImage;
        if (profileImageKey != null) this.profileImageKey = profileImageKey;
        if (dto.getFamilyId() != null) this.family = family;
        if (dto.getLeaderId() != null) this.leader = leader;

    }


    // 주소 update 전용
    // RecipientRequestAddressDto의 필드를 Recipient 엔티티에 적용하여 정보 업데이트
    public void updateWithRecipientAddressRequestDto(RecipientAddressUpdateDto dto, Institution institution) {
        if (dto.getDeliveryType() != null) this.deliveryType = dto.getDeliveryType();
        if (dto.getAddress() != null) this.address = dto.getAddress();
        if (dto.getAddressDetail() != null) this.addressDetail = dto.getAddressDetail();
        if (dto.getPostalCode() != null) this.postalCode = dto.getPostalCode();
        if (institution != null) this.institution = institution;
    }


    // 주소 update 전용
    public RecipientAddressUpdateDto toRecipientAddressResponseDto(RecipientAddressUpdateDto dto) {
        return RecipientAddressUpdateDto.builder()
                .deliveryType(this.deliveryType)
                .recipientName(this.name)
                .recipientPhone(this.phone)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .institutionName(this.institution != null ? this.institution.getName() : null)
                .institutionPhone(this.institution != null ? this.institution.getPhone() : null)
                .code(this.institution != null ? this.institution.getCode() : null)
                .build();
    }


    // 전체 정보 get 전용
    public RecipientResponseDto toRecipientResponseDto() {
        return RecipientResponseDto.builder()
                .id(this.id)
                .familyId(this.family != null ? this.family.getId() : null)
                .leaderId(this.leader != null ? this.leader.getId() : null)
                .name(this.name)
                .birth(this.birth)
                .profileImage(this.profileImage)
                .profileImageKey(this.profileImageKey)
                .calendarType(this.calendarType)
                .phone(this.phone)
                .address(toRecipientAddressResponseDtoFromEntity()) // 아래 메서드 참조
                .build();
    }

    // 전체 정보 get 전용
    public RecipientAddressUpdateDto toRecipientAddressResponseDtoFromEntity() {
        return RecipientAddressUpdateDto.builder()
                .deliveryType(this.deliveryType)
                .recipientName(this.name) // 실제 저장된 name
                .recipientPhone(this.phone) // 실제 저장된 phone
                .address(this.address)
                .addressDetail(this.addressDetail)
                .postalCode(this.postalCode)
                .institutionName(this.institution != null ? this.institution.getName() : null)
                .institutionPhone(this.institution != null ? this.institution.getPhone() : null)
                .code(this.institution != null ? this.institution.getCode() : null)
                .build();
    }

    public void assignFamily(Family family) {
        this.family = family;
    }

}


