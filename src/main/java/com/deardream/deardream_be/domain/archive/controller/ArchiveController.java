package com.deardream.deardream_be.domain.archive.controller;

import com.deardream.deardream_be.domain.archive.dto.ArchiveListResponse;
import com.deardream.deardream_be.domain.archive.dto.PdfRequestDto;
import com.deardream.deardream_be.domain.archive.entity.BookmarkStatus;
import com.deardream.deardream_be.domain.archive.service.ArchiveService;
import com.deardream.deardream_be.domain.archive.service.PdfRender;
import com.deardream.deardream_be.domain.jwt.CustomUserDetails;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/archives")
public class ArchiveController {

    private final PdfRender pdfRender;
    private final PostService postService;
    private final ArchiveService archiveService;


    @Operation(summary = "PDF 테스트를 생성합니다.")
    @PostMapping("/test/generate")
    public ApiResponse<String> generatePdf(
            @RequestBody PdfRequestDto request
    ) throws Exception {
        String fileName = "test-Archive" + request.getYear() + "-" + request.getMonth() + ".pdf";

        List<PostResponseDto> postRequests = postService.getPostsByYearMonth(request.getFamilyId(), request.getYear(), request.getMonth());

        String pdfUrl = pdfRender.generatePdfFromHtml(
                fileName,
                postRequests,
                request.getFamilyId()
        );

        return ApiResponse.onSuccess(pdfUrl);
    }

    // familyId에 따라 모든 pdf 파일 가져오기
    @Operation(summary = "가족 ID에 따라 모든 소식지를 조회합니다.")
    @GetMapping("/{familyId}")
    public ApiResponse<ArchiveListResponse> getArchivesByFamily (
            @PathVariable Long familyId
    ) {
        ArchiveListResponse archives = archiveService.getAllArchives(familyId);
        return ApiResponse.onSuccess(archives);
    }

    // 즐겨찾기 기능
    @Operation(summary = "소식지에 대한 즐겨찾기를 생성합니다.")
    @PostMapping("/{archiveId}/bookmark")
    public ApiResponse<BookmarkStatus> toggleBookmark(
            @RequestParam Long userId,
            @PathVariable @NotNull Long archiveId
            ) {
        return ApiResponse.onSuccess(
                archiveService.addBookmark(userId, archiveId)
        );
    }

    // 즐겨찾기 조회
    @Operation(summary = "소식지에 대한 즐겨찾기를 조회합니다.")
    @GetMapping("/bookmark")
    public ApiResponse<List<Long>> getFavorites(
            @RequestParam Long userId) {
        return ApiResponse.onSuccess(
                archiveService.getAllFavorites(userId));
    }

}
