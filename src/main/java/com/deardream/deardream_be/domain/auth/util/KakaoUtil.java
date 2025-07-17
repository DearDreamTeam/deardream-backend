package com.deardream.deardream_be.domain.auth.util;

import com.deardream.deardream_be.domain.auth.dto.KakaoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class KakaoUtil {

    private final ScheduledTaskHolder scheduledTaskHolder;
    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${kakao.client-id}")
    private String client;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.logout-redirect-uri}")
    private String logoutRedirectUri;

    public KakaoUtil(ScheduledTaskHolder scheduledTaskHolder) {
        this.scheduledTaskHolder = scheduledTaskHolder;
    }

    public KakaoDto.OAuthToken getAccessToken(String accessCode) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client);
        params.add("redirect_uri", redirectUri);
        params.add("code", accessCode);

        // === 여기서 실제로 전달되는 파라미터 값 로그로 남기기 ===
        log.info("Kakao token 요청 파라미터: grant_type={}, client_id={}, redirect_uri={}, code={}",
                params.getFirst("grant_type"),
                params.getFirst("client_id"),
                params.getFirst("redirect_uri"),
                params.getFirst("code")
        );

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

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Kakao token API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch(Exception e){
            log.error("Error parsing Kakao OAuth token response", e);
            throw new RuntimeException(e);
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
                .queryParam("property_keys","[\"properties\",\"kakao_account.email\",\"kakao_account.profile\"]")
                .build()
                .encode();

        ResponseEntity<String> response = restTemplate.exchange(
                uri.toUri(), HttpMethod.GET, requestEntity, String.class);

        log.info("Kakao /v2/user/me raw response: {}", response.getBody());


        ObjectMapper om = new ObjectMapper();
        KakaoDto.KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = om.readValue(response.getBody(), KakaoDto.KakaoProfile.class);

            // 디버그용 로그
            log.info("email: {}, name: {}, profileImage: {}",
                    kakaoProfile.getKakao_account().getEmail(),
                    kakaoProfile.getKakao_account().getProfile().getNickname(),
                    kakaoProfile.getKakao_account().getProfile().getProfile_image_url()
            );

        } catch (Exception e) {
            log.error("Error parsing Kakao profile response", e);
        }

        return kakaoProfile;
    }

    // 일반 로그아웃
    public void logout(String accessToken) {

//        RestTemplate restTemplate = new RestTemplate();

        if(accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/logout",
                    HttpMethod.POST,
                    request,
                    String.class
            );
            log.info("카카오 토큰 로그아웃 응답 : status={}, body={}", response.getStatusCode(), response.getBody());
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("인증 실패(401) : 토큰이 유효하지 않거나 만료됨", e);
        }
        catch (HttpClientErrorException e){
            log.error("카카오 API 오류 : 상태 코드 {}, 응답 {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("카카오 로그아웃 실패 : " + e.getResponseBodyAsString(), e);
        }
        catch (Exception e){
            log.error("카카오 로그아웃 중 예외 발생", e);
            throw new RuntimeException("카카오 로그아웃 실패", e);
        }
    }


    // 카카오계정과 함께 로그아웃 : 계정 세션까지 만료(리다이렉트 url 반환)
    public String logoutWithKakaoAccount(String logoutRedirectUri) {

        // 카카오 rest api 키로 링크 생성
        return UriComponentsBuilder
                .fromHttpUrl("https://kauth.kakao.com/oauth/logout")
                .queryParam("client_id", client)
                .queryParam("logout_redirect_uri", logoutRedirectUri)
                .build().toUriString();
    }

}
