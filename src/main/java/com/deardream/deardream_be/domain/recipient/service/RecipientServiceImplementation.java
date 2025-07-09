package com.deardream.deardream_be.domain.recipient.service;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.institution.InstitutionRepository;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipientServiceImplementation implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    @Transactional
    public RecipientResponseDto getRecipient(Long id) {
        Recipient recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new GeneralException("Recipient not found"));
        return recipient.toRecipientResponseDto();
    }


    @Override
    @Transactional
    public RecipientResponseDto createRecipient(CustomUserDetails userDetails, RecipientRequestDto dto) {

        // 1. id로 로그인 된 사용자 조회
        User leader = userRepository.findById(userDetails.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. 로그인 된 사용자의 id를 통해 family 조회
        Family family  = leader.getFamily();
        if(family == null) {
            throw new GeneralException(ErrorStatus._FAMILY_NOT_FOUND);
        }

        // 3. institution 조회
        Institution institution = institutionRepository.findById(dto.getCode())
                    .orElse(null);

        Recipient recipient = Recipient.builder()
                .family(family)
                .leader(leader)
                .name(dto.getName())
                .birth(dto.getBirth())
                .calendarType(dto.getCalendarType())
                .phone(dto.getPhone())
                .deliveryType(dto.getDeliveryType())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .postalCode(dto.getPostalCode())
                .code(institution)
                .build();

       recipientRepository.save(recipient);

        return recipient.toRecipientResponseDto();
    }

    @Override
    @Transactional
    public RecipientResponseDto updateRecipientInfo(Long recipientId, RecipientRequestDto dto) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

        Family family = dto.getFamilyId() != null ? familyRepository.findById(dto.getFamilyId()).orElse(null) : null;
        User leader = dto.getLeaderId() != null ? userRepository.findById(dto.getLeaderId()).orElse(null) : null;
        Institution institution = dto.getCode() != null ? institutionRepository.findById(dto.getCode()).orElse(null) : null;

        recipient.updateWithRecipientRequestDto(
                dto.getName(),
                dto.getBirth(),
                dto.getCalendarType(),
                dto.getPhone(),
                dto.getDeliveryType(),
                dto.getAddress(),
                dto.getAddressDetail(),
                dto.getPostalCode(),
                family,
                leader,
                institution
        );

        return recipient.toRecipientResponseDto();
    }

    @Override
    @Transactional
    public RecipientAddressUpdateDto updateRecipientAddress(Long recipientId, RecipientAddressUpdateDto dto) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

        Institution institution = dto.getCode() != null ? institutionRepository.findById(dto.getCode()).orElse(null) : null;
        recipient.updateWithRecipientAddressRequestDto(
                dto.getDeliveryType(),
                dto.getAddress(),
                dto.getAddressDetail(),
                dto.getPostalCode(),
                institution

        );

        return recipient.toRecipientAddressResponseDto();

    };


    public RecipientResponseDto findByFamilyId(Long familyId) {
        return recipientRepository.findByFamilyId(familyId).map(Recipient::toRecipientResponseDto).orElse(null);
    }
}
