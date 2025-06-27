package com.deardream.deardream_be.domain.post.controller;

import com.deardream.deardream_be.domain.post.dto.PostRequestDto;
import com.deardream.deardream_be.domain.post.dto.PostResponseDto;
import com.deardream.deardream_be.domain.post.dto.PostUpdateDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;


    @Operation(summary = "게시글 생성", description = "게시글 내용 + 이미지")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> createPost(
            @Parameter(
                    description = "게시글 본문(JSON)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRequestDto.class)
                    )
            )
            @RequestPart("request") PostRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long postId = postService.createPost(request, images != null ? images : List.of());
        return ApiResponse.onSuccess(postId);
    }


    // 나중에 로그인 완료 시 토큰에서 user 추출 필요
    @Transactional
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }

    // test 필요
    // 나중에 로그인 완료 시 토큰에서 추출할 예정 - 리펙토링 필요
    @PutMapping("/{postId}")
    public ApiResponse<String> updatePost(
            @PathVariable Long postId,
            @RequestPart PostUpdateDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        postService.updatePost(postId, request, images);
        return ApiResponse.onSuccess("Post updated successfully");
    }

    @GetMapping("/{familyId}")
    public ApiResponse<List<PostResponseDto>> getPosts(
            @PathVariable Long familyId
    ) {
        List<PostResponseDto> response =  postService.getPosts(familyId);
        return ApiResponse.onSuccess(response);
    }
}
