package com.deardream.deardream_be.domain.archive.service;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostImageService;
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
import java.time.LocalDate;
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
    public String generatePdfFromHtml(String fileName, List<PostResponseDto> posts, Long familyId) throws Exception {


        // posts -> post.imageUrls []리스트 형식, post.authorProfileImg, post.relations, post.authorName, post.content
        Context context = new Context();
        context.setVariable("posts", posts);


        String renderedHtml = templateEngine.process("index", context);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.useFont(new ClassPathResource("templates/fonts/PretendardVariable.woff2").getFile(), "PretendardVariable");
        builder.toStream(baos);
        builder.withHtmlContent(renderedHtml, "/");
        builder.run();

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        UploadResult result =  postImageService.uploadPDF(s3Config.getPdfFolder(), fileName, baos.toByteArray());

        LocalDate now = LocalDate.now();

        MonthlyArchive archive = MonthlyArchive.builder()
                .family(family)
                .archiveYear(now.getYear())
                .archiveMonth(now.getDayOfMonth())
                .pdfUrl(result.getUrl())
                .s3Key(result.getKey())
                .deliveryStatus(DeliveryStatus.PENDING)
                .build();

        archiveRepository.save(archive);


        return postImageService.getFilesUrl(archive.getS3Key());
    }


}
