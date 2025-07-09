package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.dto.ArchiveListResponse;
import com.deardream.deardream_be.domain.archive.entity.ArchiveBookmark;
import com.deardream.deardream_be.domain.archive.entity.BookmarkStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.converter.ArchiveConverter;
import com.deardream.deardream_be.domain.archive.dto.ArchiveResponseDto;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.archive.repository.BookmarkRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArchiveService {

    private final FamilyRepository familyRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchiveConverter converter;
    private final PostImageService postImageService;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

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

    // 어드민 기능 - 배송 상태 업데이트

    // 어드민 기능 - 가정 및 기관 별 정렬 리스트

}
