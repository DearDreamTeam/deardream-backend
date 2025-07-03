package com.deardream.deardream_be.domain.recipient.service;

import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;

public interface RecipientService {
    RecipientResponseDto getRecipient(Long id);
    RecipientResponseDto createRecipient(CustomUserDetails userDetails, RecipientRequestDto recipientRequestDto);
    RecipientResponseDto updateRecipientInfo (Long id, RecipientRequestDto recipientRequestDto);
    RecipientAddressUpdateDto updateRecipientAddress(Long recipientId, RecipientAddressUpdateDto recipientAddressUpdateDto);
    RecipientResponseDto findByFamilyId(Long familyId);
}
