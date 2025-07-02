package com.deardream.deardream_be.domain.archive.service;
import com.amazonaws.services.s3.AmazonS3Client;
import com.deardream.deardream_be.domain.archive.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.common.UploadResult;
import com.deardream.deardream_be.global.config.S3Config;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class PdfRender {

    private final S3Config s3Config;
    private final PostImageService postImageService;
    private final TemplateEngine templateEngine;
    private final FamilyRepository familyRepository;
    private final ArchiveRepository archiveRepository;

    /*
    * PDF 를 메모리에 생성해서 S3에 업로드 하는 방식으로 사용 예정
     */
    public String generatePdfFromHtml(String fileName, int year, int month, List<PostResponseDto> posts, Long familyId) throws Exception {

        Context context = new Context();
        context.setVariable("year", year);
        context.setVariable("month", month);
        context.setVariable("posts", posts);

        String renderedHtml = templateEngine.process("monthly-archive", context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.useFont(new ClassPathResource("fonts/NotoSansKR-VariableFont_wght.ttf").getFile(), "NotoSansKR");
        builder.toStream(baos);
        builder.withHtmlContent(renderedHtml, "/");
        builder.run();

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        UploadResult result =  postImageService.uploadPDF(s3Config.getPdfFolder(), fileName, baos.toByteArray());

        MonthlyArchive archive = MonthlyArchive.builder()
                .family(family)
                .yearMonthType(month + "-" + year)
                .pdfUrl(result.getUrl())
                .s3Key(result.getKey())
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();

        archiveRepository.save(archive);

        return postImageService.getPreSignedUrl(archive.getS3Key());


    }


}
