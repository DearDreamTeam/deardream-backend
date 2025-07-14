package com.deardream.deardream_be.domain.recipient.service;

import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface RecipientService {
    RecipientResponseDto getRecipient(Long id);
    RecipientResponseDto createRecipient(Long kakaoId, RecipientRequestDto dto, MultipartFile profileImage);
    RecipientResponseDto updateRecipientInfo (Long KakaoId, Long receipientId, RecipientRequestDto dto, MultipartFile profileImage);
    RecipientAddressUpdateDto updateRecipientAddress(Long kakaoId, Long recipientId, RecipientAddressUpdateDto recipientAddressUpdateDto);
    RecipientResponseDto findByFamilyId(Long familyId);
}
