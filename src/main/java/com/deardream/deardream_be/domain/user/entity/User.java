package com.deardream.deardream_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String provider; // ex: "kakao"

//    private String providerId; // ex: 소셜로그인 사용자 고유 id(kakaoId)
}
