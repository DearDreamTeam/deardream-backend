package com.deardream.deardream_be.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class PostRequestDto {

    @Schema(description = "게시글 내용", example = "안녕하세요. 오늘 날씨가 좋네요.")
    @Size(max = 1000, message = "Content must be less than 1000 characters")
    private String content;

    @Schema(description = "작성자 ID", example = "1")
    private Long authorId;
    private List<MultipartFile> multipartFiles;
}
