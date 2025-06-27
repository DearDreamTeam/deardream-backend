package com.deardream.deardream_be.global.config;

import com.deardream.deardream_be.domain.jwt.JwtAuthenticationFilter;
import com.deardream.deardream_be.domain.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/users/login/kakao", "/login/**", "/oauth2/**", "/api/users/reissue", "/api/users/logout", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        System.out.println("=== SecurityFilterChain 등록됨 ===");

//                .oauth2Login((AbstractHttpConfigurer::disable)
//                .oauth2Login(Customizer.withDefaults());;

//                .oauth2Login(oauth2 -> oauth2
//                                .loginPage("/login")
//                                .defaultSuccessUrl("/welcome", true)

        // 로그인 성공 시 이동할 경로
//                );


        return http.build();
    }
}
