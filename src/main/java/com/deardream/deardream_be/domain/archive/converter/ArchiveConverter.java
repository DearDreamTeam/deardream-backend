package com.deardream.deardream_be.domain.archive.converter;

import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.dto.ArchiveResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ArchiveConverter {

    // ArchiveResponseDto로 변환하는 메서드
    // public ArchiveResponseDto toArchiveResponseDto(MonthlyArchive archive) {
    //     return ArchiveResponseDto.builder()
    //             .yearMonthType(archive.getYearMonthType())
    //             .pdfUrl(archive.getPdfUrl())
    //             .build();
    // }

    public ArchiveResponseDto toArchiveResponse(MonthlyArchive archive) {
        String yearMonth = archive.getArchiveYear() + "년" + archive.getArchiveMonth() + "월";

        return ArchiveResponseDto.builder()
                .yearMonthType(yearMonth)
                .pdfUrl(archive.getPdfUrl())
                .build();
    }



}
