package com.deardream.deardream_be.domain.jwt;
import com.deardream.deardream_be.domain.user.Role;
import com.deardream.deardream_be.domain.user.entity.User;
import com.deardream.deardream_be.domain.user.repository.UserRepository;
import com.deardream.deardream_be.global.apiPayload.code.status.ErrorStatus;
import com.deardream.deardream_be.global.apiPayload.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
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

                // 1) 토큰 파싱해서 type 클레임 확인
                Claims claims = jwtUtil.parseClaims(token);
                String type = claims.get("type", String.class);

                // 2) access 토큰이 아니면 인증 스킵
                if (!"access".equals(type)) {
                    log.info("[JwtAuthenticationFilter] '{}' 토큰이므로 인증 스킵", type);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 1) kakaoId 정보 추출
                Long kakaoId = jwtUtil.getKakaoId(token);
                // role 정보 추출
                // Role role = jwtUtil.getRole(token);
                // userId 정보 추출
                // Long userId = jwtUtil.getUserId(token);


                // 2) db에서 User 조회 -> 최신 role 꺼냄
                User user = userRepository.findByKakaoId(kakaoId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));


//                log.info("Extracted kakaoId: {}, role: {}, userId: {}, ", kakaoId, role, userId);

                // 3) CustomUserDetails 생성
                CustomUserDetails userDetails = new CustomUserDetails(user.getId(), kakaoId, user.getRole());

                // 역할 기반 권한 생성 - role (인증과 인가 문제) -> 추후 수정 (권한 생성)
//                List<GrantedAuthority> authorities = new ArrayList<>();
//                if (role != null) {
//                    // Spring Security 권한 형식: "ROLE_역할명"
//                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//                }

                // 4) 인증 객체 설정 SecurityContext에 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Authenticated kakaoId={}, role={}", kakaoId, user.getRole());

            } catch (Exception e) {
                log.error("JwtAuthenticationFilter] JWT 인증 실패: " + e.getMessage(), e);
            }

        } else{
            log.info("[JwtAuthenticationFilter] Authorization header가 없거나 Bearer로 시작하지 않음");
        }

        filterChain.doFilter(request, response);
    }
}