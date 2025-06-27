package com.deardream.deardream_be.domain.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Getter
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Getter
    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Base64 디코딩 후 키 생성
        log.info("JWT Secret: {}", secret); // 실제 값 로깅
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long kakaoId) {
        return createToken(kakaoId, accessTokenExpiration, "access");
    }

    public String createRefreshToken(Long kakaoId) {
        return createToken(kakaoId, refreshTokenExpiration, "refresh");
    }

//    private String createToken(Long kakaoId, long expiration) {
//        return Jwts.builder()
//                .setSubject(String.valueOf(kakaoId))
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
//                .compact();
//    }


//    private String createToken(Long kakaoId, long expiration) {
//        return Jwts.builder()
//                .setSubject(String.valueOf(kakaoId))
//                .signWith(signingKey, SignatureAlgorithm.HS256)
//                .compact();
//    }


    // 키 객체 사용
        private String createToken(Long kakaoId, long expiration, String type) {
        return Jwts.builder()
                .setSubject(String.valueOf(kakaoId))
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}