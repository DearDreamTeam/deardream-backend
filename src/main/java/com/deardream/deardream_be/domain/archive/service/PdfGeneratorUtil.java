package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.dto.ArchiveRequestDto;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.repository.PostImageRepository;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;


@Component
@AllArgsConstructor
public class PdfGeneratorUtil {

    // 한 달 중 마지막 날 스케쥴
    private final PostRepository postRepository;
    private final FamilyRepository familyRepository;
    private final PostImageRepository postImageRepository;
    private final PostImageService postImageService;
    private final ArchiveRepository archiveRepository;

    /*
    // pdf에 맞는 request로 바꾸는 함수
    private List<ArchiveRequestDto> createArchiveInfo(Long familyId) {

        Family familyInfo = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        List<Post> familyPosts = postRepository.findAllByFamily(familyInfo);

        // ArchiveRequestDto로 변환
        return familyPosts.stream()
                .map(post -> ArchiveRequestDto.builder()
                        .authorName(post.getAuthor().getName())
                        .relation(post.getAuthor().getRelation())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())
                        .imageUrls(
                                // pre-signed URL 로 가져와야 합니다.
                                postImageRepository.findByPost(post).stream()
                                        .map(image -> postImageService.getPreSignedUrl(image.getS3Key()))
                                        .toList()
                        )

                        .build())
                .toList();

    }

    public byte[] generatePdfToByte(String templateName, Long familyId) {

        List<ArchiveRequestDto> requests = createArchiveInfo(familyId);

        // ThymeLeaf Engine
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        engine.setTemplateResolver(resolver);

        // ThymeLeaf Context setting - 템플릿에서 사용할 변수
        Context context = new Context();
        context.setVariable("archiveRequests", requests);

        // HTML
        String html = engine.process(templateName, context);

        // PDF 생성
        String fontPath = "src/main/resources/fonts/NotoSansKR-VariableFont_wght.ttf";

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.useFont(new File(fontPath), "Noto sans KR");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);

        }
    }

    @Transactional
    public String generateAndUploadPdf(String templateName, Long familyId) {
        byte[] pdfBytes = generatePdfToByte(templateName, familyId);

        String fileName = "archive-" + familyId + "-" + System.currentTimeMillis();
        String folder = "archives";

        return postImageService.uploadPDF(folder, fileName, pdfBytes);
    }


    public File generatePdfTest(String templateName, Map<String, Object> variables) throws FileNotFoundException {
        // ThymeLeaf Engine
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setPrefix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        engine.setTemplateResolver(resolver);

        Context context = new Context();
        variables.forEach((context::setVariable));
        String html = engine.process(templateName, context);

        // PDF 생성
        String fontPath = "src/main/resources/fonts/NotoSansKR-VariableFont_wght.ttf";
        File output = new File("monthly_report.pdf");
        try (OutputStream os = new FileOutputStream(output)){
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.useFont(new File(fontPath), "Noto sans KR");
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }

        return output;


    }

    @Transactional
    public void generateAndStorePdfForFamily(Family family, String templateName, LocalDate timeStamp) {
        // 1. 이번 달
        LocalDate targetMonth = timeStamp.withDayOfMonth(1); // 매월 1일 기준 저장


        // 3. PDF 생성 및 업로드
        String url = generateAndUploadPdf(templateName, family.getId());

        // 4. 저장
        MonthlyArchive archive = MonthlyArchive.builder()
                .family(family)
                .monthYear(YearMonth.from(targetMonth))
                .pdfUrl(url)
                .deliveryStatus(DeliveryStatus.PENDING)  // 기본 상태 예시
                .build();

        archiveRepository.save(archive);
    }

     */


}
