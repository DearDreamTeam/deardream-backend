package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.dto.AdminArchive;
import com.deardream.deardream_be.domain.archive.dto.ArchiveListResponse;
import com.deardream.deardream_be.domain.archive.entity.ArchiveBookmark;
import com.deardream.deardream_be.domain.archive.entity.BookmarkStatus;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.converter.ArchiveConverter;
import com.deardream.deardream_be.domain.archive.dto.ArchiveResponseDto;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.archive.repository.BookmarkRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final FamilyRepository familyRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchiveConverter converter;
    private final PostImageService postImageService;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecipientRepository recipientRepository;

    /*
    * familyId에 따라 모든 pdf 파일 가져오기
     */
    public ArchiveListResponse getAllArchives(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        List<MonthlyArchive> archives = archiveRepository.findAllByFamily(family);

        List<ArchiveResponseDto>  archivesDto= archives.stream()
                .map(response ->{
                    String yearMonth = response.getArchiveYear() + "년" + response.getArchiveMonth() + "월";
                    String fileUrl = postImageService.getFilesUrl(response.getS3Key());

                    return ArchiveResponseDto.builder()
                            .yearMonthType(yearMonth)
                            .pdfUrl(fileUrl)
                            .deliveryStatus(response.getDeliveryStatus())
                            .build();
                }).toList();

        return ArchiveListResponse.builder()
                .count(archivesDto.size())
                .dtos(archivesDto)
                .build();

    }


    // 즐겨찾기 토글
    @Transactional
    public BookmarkStatus addBookmark(Long userId, Long archiveId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        MonthlyArchive archive =archiveRepository.findById(archiveId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARCHIVE_NOT_FOUND));

        Optional<ArchiveBookmark> bookmark = bookmarkRepository.findByUserAndArchive(user, archive);

        if(bookmark.isPresent()) {
            bookmarkRepository.delete(bookmark.get());
            return BookmarkStatus.NOT_BOOKMARKED;
        }
        else {
            bookmarkRepository.save(
                ArchiveBookmark.builder()
                    .user(user)
                    .archive(archive)
                    .build()
            );
            return BookmarkStatus.BOOKMARKED;
        }

    }

    // 즐겨찾기 조회
    public List<Long> getAllFavorites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

         return  bookmarkRepository.findAllByUser(user).stream()
                .map(bookmark -> bookmark.getArchive().getId())
                .toList();


    }


    // 어드민 기능 - 가정 및 기관 별 정렬 리스트
    public AdminArchive getDeliveriesByYearAndMonth(int year, int month) {

        List<MonthlyArchive> archives = archiveRepository.findAllByArchiveYearAndArchiveMonth(year, month);

        // HOME
        List<AdminArchive.AdminHomeArchive> homeArchives = archives.stream()
                .filter(archive -> archive.getRecipient().getDeliveryType() == DeliveryType.HOME)
                .map(converter::toHomeArchive)
                .toList();

        // INSTITUTION
        Map<Institution, List<MonthlyArchive>> institutionGroup = archives.stream()
                .filter(archive -> archive.getRecipient().getDeliveryType() == DeliveryType.INSTITUTION)
                .collect(Collectors.groupingBy(archive -> archive.getRecipient().getCode()));

        List<AdminArchive.AdminInstitutionArchive> institutionArchives = new ArrayList<>();

        for(Map.Entry<Institution, List<MonthlyArchive>> entry : institutionGroup.entrySet()) {
            Institution institution = entry.getKey();

            List<MonthlyArchive> archiveList = entry.getValue();

            List<AdminArchive.AdminFamilyInfo> familyInfos = archiveList.stream()
                    .map(converter::toInstitutionInfo)
                    .toList();

            // 일괄 처리이므로 첫 번째 값이 대표값으로 하였습니다.
            DeliveryStatus status  = archiveList.get(0).getDeliveryStatus();

            institutionArchives.add(converter.toInstitutionArchive(institution, status,familyInfos));

        };

        return AdminArchive.builder()
                .homeArchives(homeArchives)
                .institutionArchives(institutionArchives)
                .build();
    }

    // 어드민 기능 - 배송 상태 업데이트
    @Transactional
    public void updateHomeDeliverStatus(Long archiveId, DeliveryStatus deliveryStatus) {
        MonthlyArchive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARCHIVE_NOT_FOUND));

        // 이미 배송 상태가 같으면 예외 처리
        if(archive.getDeliveryStatus() == deliveryStatus) {
            throw new GeneralException(ErrorStatus._ARCHIVE_DELIVERY_STATUS_ALREADY_SAME);
        }

        // 배송 상태 업데이트
        archive.updateDeliverStatus(deliveryStatus);

    }

    @Transactional
    public void updateInstitutionDeliveryStatus(Long institutionId, DeliveryStatus deliveryStatus, Integer year, Integer month) {

        // 요청한 달에 존재하는 기관의 모든 아카이브를 가져옵니다.
        List<MonthlyArchive> archives = archiveRepository.findArchivesByInstitutionIdAndYearMonth(institutionId, year, month);

        if(archives.isEmpty()) {
            throw new GeneralException(ErrorStatus._ARCHIVE_NOT_FOUND);
        }

        for(MonthlyArchive archive : archives) {
            if(archive.getDeliveryStatus() != deliveryStatus) {
                archive.updateDeliverStatus(deliveryStatus);
            }
        }

    }


}
