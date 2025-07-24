package com.deardream.deardream_be.domain.post.entity;

import com.deardream.deardream_be.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_image")
@ToString
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // URL 경로 처럼 사용되는 버킷 경로
    private String s3Key;

    private String s3Url;


    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

}
