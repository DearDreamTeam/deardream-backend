package com.deardream.deardream_be.domain.institution.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.institution.InstitutionRepository;
import com.deardream.deardream_be.domain.institution.dto.DeleteUsersRequestDto;
import com.deardream.deardream_be.domain.institution.dto.InstitutionUserResponse;
import com.deardream.deardream_be.domain.payment.PaymentRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final FamilyRepository familyRepository;
    private final PostRepository postRepository;
    private final PaymentRepository paymentRepository;


    @Transactional
    public void deleteUserFromInstitution(DeleteUsersRequestDto request) {

        Institution institution = institutionRepository.findByCode(request.getCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INSTITUTION_CODE));

        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        family.setFamilyDeActive();


    }


    public List<InstitutionUserResponse> getUsersByInstitution(String code) {

        Institution institution = institutionRepository.findByCode(code)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INSTITUTION_CODE));

        List<Recipient> recipients = recipientRepository.findAllByInstitution(institution);

        return recipients.stream()
                .filter(recipient -> recipient.getFamily().getIsActive())
                .map(recipient -> InstitutionUserResponse.builder()
                .familyId(recipient.getFamily().getId())
                .recipientName(recipient.getName())
                .userId(recipient.getId())
                .build()).toList();

    }
}
