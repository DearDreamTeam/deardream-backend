//package com.deardream.deardream_be.domain.auth.util;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.deardream.deardream_be.domain.auth.dto.KakaoDTO;
////import hongik.Todoing.domain.auth.dto.KakaoDTO;
////import hongik.Todoing.global.apiPayload.code.status.ErrorStatus;
////import hongik.Todoing.global.apiPayload.exception.GeneralException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//@Component
//@Slf4j
//public class KakaoUtil {
//
//    @Value("${kakao.client_id}")
//    private String client;
//
//    @Value("${kakao.redirect_uri}")
//    private String redirect;
//
//    public KakaoDTO.OAuthToken requestToken(String accessCode) {
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//
//        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", "authorization_code");
//        params.add("client_id", client);
//        params.add("redirect_uri", redirect);
//        params.add("code", accessCode);
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                "https://kauth.kakao.com/oauth/token",
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );
//
//        ObjectMapper om = new ObjectMapper();
//
//        KakaoDTO.OAuthToken oAuthToken = null;
//
//        try {
//            oAuthToken = om.readValue(response.getBody(), KakaoDTO.OAuthToken.class);
//
//            log.info("oAuthToken : " + oAuthToken.getAccess_token());
//        } catch (Exception e) {
//            log.error("Error parsing Kakao OAuth token response", e);
//        }
//
//        return oAuthToken;
//    }
//
//    public KakaoDTO.KakaoProfile requestProfile(KakaoDTO.OAuthToken oAuthToken) {
//        RestTemplate restTemplate2 = new RestTemplate();
//        HttpHeaders headers2 = new HttpHeaders();
//
//        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//        headers2.add("Authorization","Bearer "+ oAuthToken.getAccess_token());
//
//        HttpEntity<MultiValueMap<String,String>> kakaoProfileRequest = new HttpEntity <>(headers2);
//
//        ResponseEntity<String> response2 = restTemplate2.exchange(
//                "https://kapi.kakao.com/v2/user/me",
//                HttpMethod.GET,
//                kakaoProfileRequest,
//                String.class);
//
//        ObjectMapper om2 = new ObjectMapper();
//        KakaoDTO.KakaoProfile kakaoProfile = null;
//        try {
//            kakaoProfile = om2.readValue(response2.getBody(), KakaoDTO.KakaoProfile.class);
//            log.info("kakaoProfile : " + kakaoProfile.getProperties().getNickname());
//        } catch (Exception e) {
//            log.error("Error parsing Kakao profile response", e);
//            throw new RuntimeException("Error parsing Kakao profile response");
////            throw new GeneralException(ErrorStatus.PASSING_ERROR);
//
//        }
//
//        return kakaoProfile;
//    }
//}



package com.deardream.deardream_be.domain.auth.util;

import com.deardream.deardream_be.domain.auth.dto.KakaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KakaoUtil {

    @Value("${kakao.client_id}")
    private String client;

    @Value("${kakao.redirect_uri}")
    private String redirect;

    public KakaoDTO.OAuthToken getAccessToken(String accessCode) {
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
        KakaoDTO.OAuthToken oAuthToken = null;

        try {
            oAuthToken = om.readValue(response.getBody(), KakaoDTO.OAuthToken.class);
            log.info("accessToken: {}", oAuthToken.getAccessToken());
        } catch (Exception e) {
            log.error("Error parsing Kakao OAuth token response", e);
        }

        return oAuthToken;
    }

    public KakaoDTO.KakaoProfile getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        ObjectMapper om = new ObjectMapper();
        KakaoDTO.KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = om.readValue(response.getBody(), KakaoDTO.KakaoProfile.class);
            log.info("nickname: {}", kakaoProfile.getProperties().getNickname());
        } catch (Exception e) {
            log.error("Error parsing Kakao profile response", e);
        }

        return kakaoProfile;
    }
}
