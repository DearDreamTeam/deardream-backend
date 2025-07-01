package com.deardream.deardream_be.domain.archive.service;

public interface ArchiveService {
    // 지정 familyId를 기준으로 pdf 생성 및 s3 업로드
    public void createAndUploadPdf(Long familyId);

    // HTML 템플릿 기반으로 pdf 생성
    public byte[] changePdfToByte(String templateName, Long familyId);

}
