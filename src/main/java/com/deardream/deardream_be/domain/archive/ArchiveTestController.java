package com.deardream.deardream_be.domain.archive;

import com.deardream.deardream_be.domain.archive.dto.PdfRequestDto;
import com.deardream.deardream_be.domain.archive.service.PdfRender;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import com.deardream.deardream_be.global.common.UploadResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class ArchiveTestController {

    private final PdfRender pdfRender;
    private final PostService postService;


    @PostMapping("/api/v1/test/generate")
    public ApiResponse<String> generatePdf(
            @RequestBody PdfRequestDto request
    ) throws Exception {
        String fileName = "test-Archive" + request.getYear() + "-" + request.getMonth() + ".pdf";

        List<PostResponseDto> postRequests = postService.getPosts(request.getFamilyId());

        String pdfUrl = pdfRender.generatePdfFromHtml(
                fileName,
                request.getYear(),
                request.getMonth(),
                postRequests,
                request.getFamilyId()
        );

        return ApiResponse.onSuccess(pdfUrl);


    }

}
