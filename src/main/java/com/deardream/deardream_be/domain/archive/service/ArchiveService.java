package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.dto.*;
import com.deardream.deardream_be.domain.archive.entity.ArchiveBookmark;
import com.deardream.deardream_be.domain.archive.entity.BookmarkStatus;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.converter.ArchiveConverter;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.archive.repository.BookmarkRepository;
import com.deardream.deardream_be.domain.common.AuditingBookmark;
import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.institution.DeliveryType;
import com.deardream.deardream_be.domain.institution.Institution;
import com.deardream.deardream_be.domain.institution.InstitutionRepository;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.domain.user.Relation;
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
    private final PostService postService;
    private final InstitutionRepository institutionRepository;
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

                    String thumbnailUrl = postService.getRandomThumbnailUrl(family, response.getArchiveYear(), response.getArchiveMonth())
                            .orElse(null);

                    return ArchiveResponseDto.builder()
                            .archiveId(response.getId())
                            .yearMonthType(yearMonth)
                            .pdfUrl(fileUrl)
                            .deliveryStatus(response.getDeliveryStatus())
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                }).toList();

        return ArchiveListResponse.builder()
                .dtos(archivesDto)
                .build();

    }


    // 즐겨찾기 토글
    @AuditingBookmark("즐겨찾기 삭제/추가")
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
    @AuditingBookmark("즐겨찾기 조회")
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
                .collect(Collectors.groupingBy(archive -> archive.getRecipient().getInstitution()));

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

    // 어드민 기능 - 개인 플랜 정보 조회
    public AdminHomeArchiveResponse getHomeArchives(int year, int month) {
        List<MonthlyArchive> archives = archiveRepository.findHomeArchives(year, month);

        List<AdminHomeArchiveResponse.HomeArchiveDto> home = archives.stream()
                .map(converter::toHomeArchiveResponse)
                .toList();

        return AdminHomeArchiveResponse.builder()
                .homeArchives(home)
                .build();
    }


    // 어드민 기능 - 기관 플랜 정보 조회
    public AdminInstitutionArchiveResponse getInstitutionArchives(String institutionCode, int year, int month) {
        List<MonthlyArchive> archives = archiveRepository.findArchivesByInstitutionCodeAndYearMonth(institutionCode, year, month);

        List<AdminInstitutionArchiveResponse.InstitutionArchiveDto> institution = archives.stream()
                .map(converter::toInstitutionArchiveResponse)
                .toList();

        return AdminInstitutionArchiveResponse.builder()
                .institutions(institution)
                .build();
    }


    // 어드민 기능 - 배송 상태 업데이트
    @Transactional
    public void updateHomeDeliverStatus(Long archiveId, DeliveryStatus  deliveryStatus) {
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
    public void updateInstitutionDeliveryStatus(String institutionCode, AdminRequestDto request) {

        // 요청한 달에 존재하는 기관의 모든 아카이브를 가져옵니다.
        List<MonthlyArchive> archives = archiveRepository.findArchivesByInstitutionCodeAndYearMonth(institutionCode, request.getYear(), request.getMonth());

        if(archives.isEmpty()) {
            throw new GeneralException(ErrorStatus._ARCHIVE_NOT_FOUND);
        }

        for(MonthlyArchive archive : archives) {
            if(archive.getDeliveryStatus() != request.getDeliveryStatus()) {
                archive.updateDeliverStatus(request.getDeliveryStatus());
            }
        }

    }

    // 어드민 기능 - 기관 전체 조회
    public InstitutionResponseDto getAllInstitutionsInfo(AdminArchiveRequest request) {

        int year = request.getYear();   // 연도
        int month = request.getMonth(); // 월

        // 해당 연/월의 archive 중, INSTITUTION 대상만 조회
        List<MonthlyArchive> monthlyArchives = archiveRepository.findAllByArchiveYearAndArchiveMonth(year, month)
                .stream()
                .filter(archive -> archive.getRecipient().getDeliveryType() == DeliveryType.INSTITUTION)
                .toList();

        // Institution -> 그 달의 대표 DeliveryStatus 매핑
        Map<Institution, DeliveryStatus> institutionStatusMap = new HashMap<>();

        for (MonthlyArchive archive : monthlyArchives) {
            Institution institution = archive.getRecipient().getInstitution();
            if (institution != null && !institutionStatusMap.containsKey(institution)) {
                institutionStatusMap.put(institution, archive.getDeliveryStatus()); // 첫번째 걸 대표로
            }
        }

        // 모든 Institution 가져와서, 있으면 상태 매핑
        List<Institution> allInstitutions = institutionRepository.findAll();

        List<InstitutionResponseDto.InstitutionInfo> infos = allInstitutions.stream()
                .map(inst -> {
                    DeliveryStatus status = institutionStatusMap.getOrDefault(inst, null); // 없으면 null
                    return converter.inInstitutionResponseInfo(inst, status);
                })
                .toList();

        return InstitutionResponseDto.builder()
                .institutionInfoList(infos)
                .build();

    }

    public ArchiveInfoDto getArchiveFamilyInfo(int Year, int month, Long familyId) {

        // User들 중 familyId로 찾기
        List<User> users = userRepository.findAllByFamilyId(familyId);

        // ArchiveInfoDto 생성
        List<ArchiveInfoDto.AuthorInfoDto> authorInfoDtos = users.stream()
                .map(user -> ArchiveInfoDto.AuthorInfoDto.builder()
                        .authorName(user.getName())
                        .relation(user.getRelation()!= Relation.OTHER ? user.getRelation().getDescription() : user.getOtherRelation())
                        .build())
                .toList();

        // ArchiveInfoDto 반환
        return ArchiveInfoDto.builder()
                .year(Year)
                .month(month)
                .authors(authorInfoDtos)
                .build();

    }


}
