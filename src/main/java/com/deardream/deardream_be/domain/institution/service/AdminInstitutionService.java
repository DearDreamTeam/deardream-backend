package com.deardream.deardream_be.domain.institution.service;

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

    /*
    @Transactional
    public void deleteUserFromInstitution(DeleteUsersRequestDto request) {

        Institution institution = institutionRepository.findByCode(request.getCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INSTITUTION_CODE));

        Recipient recipient = recipientRepository.findById(request.getUserId())
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

        List<User> familyUsers = userRepository.findAllByFamilyId(recipient.getFamily().getId());

        // 추방 시, 수신자의 정보와 함께 가족 테이블도 삭제됨
        for (User user : familyUsers) {
            user.deleteFamily();
        }
        // 여기 때문에 db 수정이 필요합니다.
        paymentRepository.deleteAllByUser(recipient.getLeader());
        postRepository.deleteAllByFamily(recipient.getFamily());
        recipientRepository.deleteById(request.getUserId());
        familyRepository.deleteById(recipient.getFamily().getId());


    }

     */

    public List<InstitutionUserResponse> getUsersByInstitution(String code) {

        Institution institution = institutionRepository.findByCode(code)
                .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_INSTITUTION_CODE));

        List<Recipient> recipients = recipientRepository.findAllByInstitution(institution);

        return recipients.stream().map(recipient -> InstitutionUserResponse.builder()
                .familyId(recipient.getFamily().getId())
                .recipientName(recipient.getName())
                .userId(recipient.getId())
                .build()).toList();

    }
}
