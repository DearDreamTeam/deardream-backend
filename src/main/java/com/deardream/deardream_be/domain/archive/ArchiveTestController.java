package com.deardream.deardream_be.domain.archive;

import com.deardream.deardream_be.domain.archive.service.ArchivePdfScheduler;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ArchiveTestController {

    private final ArchivePdfScheduler archivePdfScheduler;

    // 테스트용으로 PDF 생성 스케쥴러를 실행하는 엔드포인트
    @GetMapping("/api/v1/test/archive")
    public ApiResponse<Void> testArchive() {
        archivePdfScheduler.testPdfForAllFamilies();
        return ApiResponse.onSuccess(null);
    }

}
