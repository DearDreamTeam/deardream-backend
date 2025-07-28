package com.deardream.deardream_be.domain.archive.service;
import com.deardream.deardream_be.domain.archive.entity.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.entity.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.entity.Family;
import com.deardream.deardream_be.domain.family.repository.FamilyRepository;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.recipient.entity.Recipient;
import com.deardream.deardream_be.domain.recipient.repository.RecipientRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
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
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class PdfRender {

    private final S3Config s3Config;
    private final PostImageService postImageService;
    private final TemplateEngine templateEngine;
    private final FamilyRepository familyRepository;
    private final ArchiveRepository archiveRepository;
    private final RecipientRepository recipientRepository;

    /*
    * PDF 를 메모리에 생성해서 S3에 업로드 하는 방식으로 사용 예정
     */
    public String generatePdfFromHtml(String fileName, List<PostResponseDto> posts, Long familyId) throws Exception {


        // posts -> post.imageUrls []리스트 형식, post.authorProfileImg, post.relations, post.authorName, post.content
        Context context = new Context();
        context.setVariable("posts", posts);

        ClassPathResource cssFile = new ClassPathResource("templates/style.css");
        String cssContent = new String(cssFile.getInputStream().readAllBytes());

        String renderedHtml = templateEngine.process("index", context);
        renderedHtml = renderedHtml.replaceAll("(?i)<link[^>]*href=[\"'][^\"']*style\\.css[\"'][^>]*/?>", "");


        String resultHtml = renderedHtml.replace("</head>", "<style>" + cssContent + "</style></head>");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();

        ClassPathResource fontResource = new ClassPathResource("templates/fonts/PretendardVariable.ttf");
        File tempFontFile = File.createTempFile("Pretendard", ".ttf");
        tempFontFile.deleteOnExit();
        try (InputStream fontStream = fontResource.getInputStream()) {
            Files.copy(fontStream, tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        builder.useFont(tempFontFile, "Pretendard");
        builder.toStream(baos);
        builder.withHtmlContent(resultHtml, "/");
        builder.run();

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        Recipient recipient = recipientRepository.findSingleByFamilyId(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._RECIPIENT_NOT_FOUND));

        UploadResult result =  postImageService.uploadPDF(s3Config.getPdfFolder(), fileName, baos.toByteArray());

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Optional<MonthlyArchive> existingArchive = archiveRepository.findByFamilyAndArchiveYearAndArchiveMonth(family, now.getYear(), now.getMonthValue());

        MonthlyArchive archive;

        if(existingArchive.isPresent()) {
            // 기존 아카이브 덮어쓰기
            archive = existingArchive.get();
            archive.updatePdfUrl(result.getUrl(),result.getKey());

        } else {
            archive = MonthlyArchive.builder()
                    .family(family)
                    .archiveYear(now.getYear())
                    .archiveMonth(now.getMonthValue())
                    .pdfUrl(result.getUrl())
                    .s3Key(result.getKey())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .recipient(recipient) // recipient는 나중에 설정할 예정
                    .build();
        }


        archiveRepository.save(archive);

        return postImageService.getFilesUrl(archive.getS3Key());
    }


}
