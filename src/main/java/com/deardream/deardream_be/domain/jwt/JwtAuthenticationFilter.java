package com.deardream.deardream_be.domain.jwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        System.out.println("=== JwtAuthenticationFilter 생성됨 ===");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        // 디버그용 로그 추가
        log.info("Authorization header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " 제거

            // 디버그용 로그 추가
            log.info("Bearer token: " + token);

            try {
                // 키 객체 사용 (바이트배열 안씀)
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtUtil.getSigningKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                Long kakaoId = Long.valueOf(claims.getSubject());

                // 사용자 인증 객체 생성 후 SecurityContext에 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(kakaoId, null, Collections.emptyList());
//                authentication.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("JwtAuthenticationFilter] JWT 인증 실패: " + e.getMessage(), e);
            }

        } else{
            log.info("[JwtAuthenticationFilter] Authorization header가 없거나 Bearer로 시작하지 않음");
        }

        filterChain.doFilter(request, response);
    }
}
