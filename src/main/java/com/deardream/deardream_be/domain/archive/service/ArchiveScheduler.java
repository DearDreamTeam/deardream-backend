package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.dto.ArchiveInfoDto;
import com.deardream.deardream_be.domain.archive.service.PdfRender;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@AllArgsConstructor
public class ArchiveScheduler {

    private final FamilyRepository familyRepository;
    private final PostService postService;
    private final PdfRender pdfRender;
    private final ArchiveService archiveService;

    // 테스트용으로 매월 1일 자정에 실행되는 스케줄러
    // 날짜는 다시 설정해야 합니다.
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정에 실행
    public void testArchive() throws Exception {
        List<Long> familyIds = familyRepository.findAllFamilyIds();

        // ex) 2025-07-02
        LocalDate now  = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate target = now.minusMonths(1); // → 2025-06-01

        int targetYear = target.getYear();
        int targetMonth = target.getMonthValue();

        // 모든 familyId에 대해서 실행
        for (Long familyId : familyIds) {
            String fileName = "Archive" + familyId + now + ".pdf";
            List<PostResponseDto> postRequests = postService.getPostsByYearMonth(familyId, targetYear, targetMonth);

            ArchiveInfoDto archiveInfoDto = archiveService.getArchiveFamilyInfo(targetYear, targetMonth, familyId);

            pdfRender.generatePdfFromHtml(
                    fileName,
                    postRequests,
                    archiveInfoDto,
                    familyId
            );

            return ;

        }


    }
}
