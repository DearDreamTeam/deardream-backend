package com.deardream.deardream_be.domain.post;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.user.entity.User;
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

    // 원본 파일 이름과 서버 저장 파일 경로 분리
    // 동일 이름 파일 업로드 시 오류 발생
    @Column(unique = true, nullable = false)
    private String fileName;

    // URL 경로 처럼 사용되는 버킷 경로
    private String s3Key;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

}
