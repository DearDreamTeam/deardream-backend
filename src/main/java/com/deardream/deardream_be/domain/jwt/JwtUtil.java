package com.deardream.deardream_be.domain.jwt;

import com.deardream.deardream_be.domain.user.Role;
import io.jsonwebtoken.Claims;
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

    /**
     * 임시 토큰 만료 시간 (밀리초 단위).
     * application.yml에 없으면 기본 5분(300_000ms) 사용.
     */
    @Value("${jwt.temp-token-expiration:300000}")
    private long tempTokenExpiration;

    @Getter
    private final Key signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Base64 디코딩 후 키 생성
        log.info("JWT Secret: {}", secret); // 실제 값 로깅
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long kakaoId, Role role, Long userId) {
        return createToken(kakaoId, role, userId, accessTokenExpiration, "access");
    }

    public String createRefreshToken(Long kakaoId, Role role, Long userId) {
        return createToken(kakaoId, role, userId, refreshTokenExpiration, "refresh");
    }


    // 키 객체 사용
        private String createToken(Long kakaoId, Role role, Long userId, long expiration, String type) {
        return Jwts.builder()
                .setSubject(String.valueOf(kakaoId))
                .claim("type", type)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }


    // 임시 JWT 토큰(프로필 등록 전용)
    // 로그인 직후 프로필 미완료 사용자에게만 발급할 임시 jwt
    public String createTempToken(Long kakaoId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(kakaoId))
                .claim("type", "temp")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tempTokenExpiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }


    // userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Long.class);
    }

    // role 추출
    public Role getRole(String token) {
        Claims claims = parseClaims(token);
        return Role.valueOf(claims.get("role", String.class));
    }

    // kakaoId 추출
    public Long getKakaoId(String token){
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}