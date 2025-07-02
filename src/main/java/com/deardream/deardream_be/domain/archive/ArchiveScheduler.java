package com.deardream.deardream_be.domain.archive;

import com.deardream.deardream_be.domain.archive.service.PdfRender;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class ArchiveScheduler {

    private final FamilyRepository familyRepository;
    private final PostService postService;
    private final PdfRender pdfRender;

    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정에 실행
    public void testArchive() throws Exception {
        List<Long> familyIds = familyRepository.findAllFamilyIds();

        // ex) 2025-07-02
        LocalDate now  = LocalDate.now();

        // 모든 familyId에 대해서 실행
        for (Long familyId : familyIds) {
            String fileName = "Archive" + familyId + now + ".pdf";
            List<PostResponseDto> postRequests = postService.getPosts(familyId);

            pdfRender.generatePdfFromHtml(
                    fileName,
                    now.getYear(),
                    now.getMonthValue(),
                    postRequests,
                    familyId
            );

            return ;

        }





    }
}
