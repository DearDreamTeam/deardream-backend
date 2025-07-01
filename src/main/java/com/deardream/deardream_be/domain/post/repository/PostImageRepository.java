package com.deardream.deardream_be.domain.post.repository;

import com.deardream.deardream_be.domain.post.Post;
import com.deardream.deardream_be.domain.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPost(Post post);

    // post에 맞는 PostImage를 찾아 s3Key 반환
    @Query("SELECT p.s3Key FROM PostImage p WHERE p.post = :post")
    List<String> findS3KeysByPost(@Param("post") Post post);
}
