package com.deardream.deardream_be.domain.recipient.service;

import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.institution.InstitutionRepository;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.domain.recipient.dto.RecipientAddressUpdateDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientRequestDto;
import com.deardream.deardream_be.domain.recipient.dto.RecipientResponseDto;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.common.UploadResult;
import com.deardream.deardream_be.global.config.S3Config;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RecipientServiceImplementation implements RecipientService {

    private final RecipientRepository recipientRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final S3Config s3Config;
    private final PostImageService postImageService;


    @Override
    @Transactional
    public RecipientResponseDto getRecipient(Long id) {
//        Recipient recipient = recipientRepository.findById(id)
//                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));
        Recipient recipient = recipientRepository.findByLeaderId(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));
        return recipient.toRecipientResponseDto();
    }


    @Override
    @Transactional
    public RecipientResponseDto createRecipient(Long kakaoId, RecipientRequestDto dto, MultipartFile profileImage) {

        // 1. id로 로그인 된 사용자 조회
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 1-1. 이미 등록된 수신자가 있는지 사전 검사
        boolean recipientExists = recipientRepository.existsByLeaderId(user.getId());
        if (recipientExists) {
            throw new GeneralException(ErrorStatus._RECIPIENT_ALREADY_REGISTERED);
        }

        user.joinRecipientMakerAsLeader();
        userRepository.save(user);

        // 2. 로그인 된 사용자의 id를 통해 family 조회
        Family family  = user.getFamily();

        // 3. institution 조회
        Institution institution = institutionRepository.findByCode(dto.getAddress().getCode())
                .orElse(null);

        String profileImageUrl = null;
        String profileImageKey = null;

        if(profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);

            String fileName = "recipient_profile_" + kakaoId + "_" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            UploadResult uploadResult = postImageService.uploadFile(s3Config.getProfileFolder(), fileName, profileImage);

            profileImageKey = uploadResult.getKey();
            profileImageUrl = postImageService.getFilesUrl(profileImageKey);
        }


        Recipient recipient = Recipient.builder()
                .family(family)
                .leader(user)
                .name(dto.getName())
                .birth(dto.getBirth())
                .profileImage(profileImageUrl)
                .profileImageKey(profileImageKey)
                .calendarType(dto.getCalendarType())
                .phone(dto.getPhone())
                .deliveryType(dto.getAddress().getDeliveryType())
                .address(dto.getAddress().getAddress())
                .addressDetail(dto.getAddress().getAddressDetail())
                .postalCode(dto.getAddress().getPostalCode())
                .institution(institution)
                .build();

        // 리더 역할 부여
       recipientRepository.save(recipient);

       return recipient.toRecipientResponseDto(dto.getAddress(), profileImageUrl, profileImageKey);
    }

    @Override
    @Transactional
    public RecipientResponseDto updateRecipientInfo(Long kakaoId, Long recipientId, RecipientRequestDto dto, MultipartFile profileImage) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        user.joinRecipientMakerAsLeader();
        userRepository.save(user);

        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

//        Family family = dto.getFamilyId() != null ? familyRepository.findById(dto.getFamilyId()).orElse(null) : null;
        User leader = dto.getLeaderId() != null ? userRepository.findById(dto.getLeaderId()).orElse(null) : null;

        // 기존 프로필 이미지 삭제
        if(recipient.getProfileImageKey() != null) {
            postImageService.deleteFile(recipient.getProfileImageKey());
        }
        // 새 프로필 이미지 업로드
        String profileImageUrl = null;
        String profileImageKey = null;

        if(profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);

            String fileName = "recipient_profile_" + recipientId + "_" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            UploadResult uploadResult = postImageService.uploadFile(s3Config.getProfileFolder(), fileName, profileImage);

            profileImageKey = uploadResult.getKey();
            profileImageUrl = postImageService.getFilesUrl(profileImageKey);
        }

        recipient.updateWithRecipientRequestDto(dto, profileImageUrl, profileImageKey);

        recipientRepository.save(recipient);

        return recipient.toRecipientResponseDto(dto.getAddress(), profileImageUrl, profileImageKey);
    }

    @Override
    @Transactional
    public RecipientAddressUpdateDto updateRecipientAddress(Long kakaoId, Long recipientId, RecipientAddressUpdateDto dto) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        user.joinRecipientMakerAsLeader();
        userRepository.save(user);

        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

        Institution institution = dto.getCode() != null ? institutionRepository.findByCode(dto.getCode()).orElse(null) : null;

        recipient.updateWithRecipientAddressRequestDto(dto, institution);

        recipientRepository.save(recipient);

        return recipient.toRecipientAddressResponseDto(dto);

    };


    public RecipientResponseDto findByFamilyId(Long familyId) {
        if(familyId == null) {
            return null;
        }
        return recipientRepository.findByFamilyId(familyId).map(Recipient::toRecipientResponseDto).orElse(null);
    }


    private void validateProfileImage(MultipartFile profileImage) {
        // 파일 크기 제한 (1MB)
        long maxSizeBytes = 1024 * 1024;
        if (profileImage.getSize() > maxSizeBytes) {
            throw new GeneralException(ErrorStatus._IMAGE_SIZE_EXCEEDED);
        }

    }
}
