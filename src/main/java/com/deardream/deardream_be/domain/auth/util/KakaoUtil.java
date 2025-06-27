package com.deardream.deardream_be.domain.auth.util;

import com.deardream.deardream_be.domain.auth.dto.KakaoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class KakaoUtil {

    @Value("${kakao.client-id}")
    private String client;

    @Value("${kakao.redirect-uri}")
    private String redirect;

    public KakaoDto.OAuthToken getAccessToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client);
        params.add("redirect_uri", redirect);
        params.add("code", accessCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        ObjectMapper om = new ObjectMapper();
        KakaoDto.OAuthToken oAuthToken = null;

        try {
            oAuthToken = om.readValue(response.getBody(), KakaoDto.OAuthToken.class);
            log.info("accessToken: {}", oAuthToken.getAccess_token());

        } catch (Exception e) {
            log.error("Error parsing Kakao OAuth token response", e);
        }

        return oAuthToken;
    }

    public KakaoDto.KakaoProfile getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);


        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl("https://kapi.kakao.com/v2/user/me")
                .queryParam("property_keys","[\"kakao_account.name\",\"kakao_account.email\",\"kakao_account.profile\"]")
                .build()
                .encode();

        ResponseEntity<String> response = restTemplate.exchange(
                uri.toUri(), HttpMethod.GET, requestEntity, String.class);


        ObjectMapper om = new ObjectMapper();
        KakaoDto.KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = om.readValue(response.getBody(), KakaoDto.KakaoProfile.class);

            // 디버그용 로그
            log.info("email: {}, name: {}, profileImage: {}",
                    kakaoProfile.getKakao_account().getEmail(),
                    kakaoProfile.getKakao_account().getName(),
                    kakaoProfile.getKakao_account().getProfile().getProfile_image_url()
            );

        } catch (Exception e) {
            log.error("Error parsing Kakao profile response", e);
        }

        return kakaoProfile;
    }
}
