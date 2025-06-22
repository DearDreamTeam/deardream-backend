package com.deardream.deardream_be.domain.post.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @Size(max = 1000, message = "Content must be less than 1000 characters")
    private String content;
    private String authorId;
    private MultipartFile multipartFile;
}
