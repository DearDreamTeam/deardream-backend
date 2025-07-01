package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.DeliveryStatus;
import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchivePdfScheduler {

    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final FamilyRepository familyRepository;
    private final ArchiveService archiveService;

    /*
    * 매월 마지막 날 자정에 PDF 자동 생성 및 업로드
    * cron: 초 분 시 일 월 요일 (매월 마지막 날 23:59:00)
     */

    /*
    @Scheduled(cron = "0 59 23 L * ?")  // 매월 마지막 날 23:59
    public void generateMonthlyArchivePdfs() {
        LocalDate executionTime = LocalDate.now();

        List<Family> families = familyRepository.findAll();

        for (Family family : families) {
            try {
                pdfGeneratorUtil.generateAndStorePdfForFamily(family, "archive-multi-pdf-template", executionTime);
                System.out.println("[✅ 저장 완료] 가족 ID: " + family.getId());
            } catch (IllegalStateException dup) {
                System.out.println("[⚠️ 이미 존재] 가족 ID: " + family.getId());
            } catch (Exception e) {
                System.err.println("[❌ 실패] 가족 ID: " + family.getId() + " - 이유: " + e.getMessage());
            }
        }
    }

     */

    // 매월 1일 오전 1시에 아카이브 PDF 생성
    @Scheduled(cron = "0 0 1 1 * *")
    public void generatePdfForAllFamilies() {
        List<Long> allFamilyIds = familyRepository.findAllFamilyIds();

        for(Long familyId : allFamilyIds) {
            archiveService.createAndUploadPdf(familyId);
        }
    }

    public void testPdfForAllFamilies() {
        List<Long> allFamilyIds = familyRepository.findAllFamilyIds();

        for(Long familyId : allFamilyIds) {
            archiveService.createAndUploadPdf(familyId);
        }
    }


}

