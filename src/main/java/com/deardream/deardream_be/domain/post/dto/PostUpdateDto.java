package com.deardream.deardream_be.domain.post.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDto {
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    private String content;
    private List<MultipartFile> multipartFiles;
}
