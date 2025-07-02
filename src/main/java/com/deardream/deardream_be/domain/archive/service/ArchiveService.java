package com.deardream.deardream_be.domain.archive.service;

import com.deardream.deardream_be.domain.archive.ArchiveTestController;
import com.deardream.deardream_be.domain.archive.MonthlyArchive;
import com.deardream.deardream_be.domain.archive.converter.ArchiveConverter;
import com.deardream.deardream_be.domain.archive.dto.ArchiveResponseDto;
import com.deardream.deardream_be.domain.archive.repository.ArchiveRepository;
import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.family.FamilyRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArchiveService {

    private final FamilyRepository familyRepository;
    private final ArchiveRepository archiveRepository;
    private final ArchiveConverter converter;

    /*
    * familyId에 따라 모든 pdf 파일 가져오기
     */
    public List<ArchiveResponseDto> getAllArchives(Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._FAMILY_NOT_FOUND));

        List<MonthlyArchive> archives = archiveRepository.findAllByFamily(family);

        return archives.stream()
                .map(converter::toArchiveResponse)
                .collect(Collectors.toList());

    }


    /*
    * 즐겨찾기 기능
     */

    // 어드민 기능 - 배송 상태 업데이트

    // 어드민 기능 - 가정 및 기관 별 정렬 리스트
    public List<>
}
