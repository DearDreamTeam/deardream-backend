package com.deardream.deardream_be.domain.post.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.deardream.deardream_be.domain.archive.service.PdfGeneratorUtil;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import com.deardream.deardream_be.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostImageService {

    private final AmazonS3Client amazonS3Client;
    private final S3Config s3Config;
    //private final PdfGeneratorUtil pdfGeneratorUtil;

    @Transactional
    public String uploadFile(String folder, String fileName, MultipartFile file) {
        String uniqueFileName = UUID.randomUUID() + "-" + fileName;
        String key = folder + "/" + uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try (InputStream inputStream = file.getInputStream()){
            amazonS3Client.putObject(s3Config.getBucket(), key, inputStream, metadata);

        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }

        return amazonS3Client.getUrl(s3Config.getBucket(), key).toString();
    }


    @Transactional
    public String uploadPDF(String folder, String fileName, byte[] pdfBytes) {
        String uniqueFileName = UUID.randomUUID() + "-" + fileName;
        String key = folder + "/" + uniqueFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");
        metadata.setContentLength(pdfBytes.length);

        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes)) {
            amazonS3Client.putObject(s3Config.getBucket(), key, inputStream, metadata);
        } catch (IOException e) {
            throw new RuntimeException("PDF upload failed: " + e.getMessage(), e);
        }

        return amazonS3Client.getUrl(s3Config.getBucket(), key).toString();
    }

    @Transactional
    public void deleteFile(String fileUrl) {
        try {
            String splitString = ".com/";
            String key = fileUrl.substring(fileUrl.lastIndexOf(splitString) + splitString.length());
            amazonS3Client.deleteObject(new DeleteObjectRequest(s3Config.getBucket(), key));
        } catch (SdkClientException e) {
            throw new GeneralException(ErrorStatus._S3_DELETE_ERROR);
        }
    }

    public String getPreSignedUrl(String s3Key) {
        // 20분 설정
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 20);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(s3Config.getBucket(), s3Key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        URL preSignedUrl = amazonS3Client.generatePresignedUrl(request);
        return preSignedUrl.toString();
    }
}
