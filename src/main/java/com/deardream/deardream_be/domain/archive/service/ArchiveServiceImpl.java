package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.dto.ArchiveRequest;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.PostImage;
import com.deardream.deardream_be.domain.post.repository.PostRepository;
import com.deardream.deardream_be.domain.post.service.PostImageService;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.config.S3Config;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ArchiveServiceImpl implements ArchiveService{

    private final PostRepository postRepository;
    private final ArchiveRepository archiveRepository;
    private final FamilyRepository familyRepository;
    private final S3Config s3Config;
    private final PostImageService postImageService;
    @Override
    @Transactional
    public void createAndUploadPdf(Long familyId) {

        YearMonth thisMonth = YearMonth.now();

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        // 중복 방지
        if(archiveRepository.existsByFamilyAndMonthYear(family, thisMonth)) {
            throw new GeneralException(ErrorStatus._ARCHIVE_ALREADY_EXISTS);
        }

        try {

            System.out.println("PDF 생성 시작...");

            String templateName = "monthly-archive"; // 템플릿 이름 설정
            byte[] pdfBytes = changePdfToByte(templateName, familyId);

            System.out.println("PDF 생성 완료: " + pdfBytes.length + " bytes");

            String fileName = family.getId() +  "-" +  thisMonth.toString() + ".pdf";
            System.out.println("PDF 파일 이름: " + fileName);

            String s3Url = postImageService.uploadPDF(s3Config.getPdfFolder(), fileName, pdfBytes);
            System.out.println("PDF S3 업로드 완료: " + s3Url);

            // Archive 엔티티 생성 및 저장
            MonthlyArchive archive = MonthlyArchive.builder()
                    .family(family)
                    .monthYear(thisMonth)
                    .pdfUrl(s3Url)
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .build();

            archiveRepository.save(archive);

            System.out.println("Archive 엔티티 저장 완료: " + archive.getId());


        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._PDF_UPLOAD_ERROR);
        }

    }

    /*
    * 지정된 데이터 및 템플릿으로
    * 템플릿 렌더링 + PDF 변환
     */
    @Override
    @Transactional
    public byte[] changePdfToByte(String templateName, Long familyId) {

        // 게시글 데이터 수집
        List<ArchiveRequest> requests = createArchiveInfo(familyId);

        System.out.println("ArchiveRequest 리스트 생성 완료: " + requests.size() + " items");

        // ThymeLeaf 템플릿 설정
        TemplateEngine engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        engine.setTemplateResolver(resolver);

        System.out.println("ThymeLeaf 템플릿 엔진 설정 완료");

        // 템플릿에 데이터 반영
        Context context = new Context();
        context.setVariable("archiveRequests", requests);

        System.out.println("ThymeLeaf Context 설정 완료: " + requests.size() + " items");

        // HTML 생성
        String htmlContent = engine.process(templateName, context);

        // PDF 렌더링
        String fontPath = "src/main/resources/fonts/NotoSansKR-VariableFont_wght.ttf";

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.useFont(new File(fontPath), "Noto Sans KR");
            builder.toStream(os);
            builder.run();

            return os.toByteArray();

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._PDF_GENERATION_ERROR);
        }

    }


    /*
    * 지정된 familyId 사용자들이
    * 이번 달 (예: 2025-06) 작성한 게시글만 필터링하여
    * ArchiveRequest 리스트로 가공
     */
    private List<ArchiveRequest> createArchiveInfo(Long familyId) {

        YearMonth thisMonth = YearMonth.now();
        LocalDate startOfMonth = thisMonth.atDay(1);
        LocalDate endOfMonth = thisMonth.atEndOfMonth();

        List<Post> posts = postRepository.findPostsForMonth(familyId, startOfMonth, endOfMonth);

        return posts.stream().map(post -> {
            User user = post.getAuthor();

            List<String> imageUrls = post.getImages()
                    .stream()
                    .map(PostImage::getS3Url)
                    .toList();

            return new ArchiveRequest(
                    user.getProfileImageUrl(),
                    user.getName(),
                    user.getRelation(),
                    imageUrls,
                    post.getContent(),
                    post.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    private boolean isScheduledDate(LocalDate today) {
        return true;
    }
}
