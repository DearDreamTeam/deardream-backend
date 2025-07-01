package com.deardream.deardream_be.domain.user.entity;

import com.deardream.deardream_be.domain.family.Family;
import com.deardream.deardream_be.domain.user.Relation;
import com.deardream.deardream_be.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String provider; // ex: "kakao"

    // 이거 추가되어야 합니다.
    private String profileImageUrl;

    private Relation relation;
//    private String providerId; // ex: 소셜로그인 사용자 고유 id(kakaoId)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;
}
