package com.deardream.deardream_be.domain.post.controller;

import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.dto.PostRequestDto;
import com.deardream.deardream_be.domain.post.dto.PostUpdateDto;
import com.deardream.deardream_be.domain.post.service.PostService;
import com.deardream.deardream_be.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<Long> createPost(
            @RequestParam PostRequestDto request,
            @RequestPart(required = false) List<MultipartFile> images
            ) {
        Long postId = postService.createPost(request, images != null ? images : List.of());
        return ApiResponse.onSuccess(postId);
    }

    @Transactional
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }

    @PutMapping("/{postId}")
    public ApiResponse<String> updatePost(
            @PathVariable Long postId,
            @RequestPart PostUpdateDto request
    ) {
        postService.updatePost(postId, request);
        return ApiResponse.onSuccess("Post updated successfully");
    }




}
