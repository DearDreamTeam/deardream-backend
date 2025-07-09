package com.deardream.deardream_be.domain.auth.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import org.springframework.context.annotation.Profile;

public class KakaoDto {

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        private Long id;
        private String connected_at;
        private Properties properties;
        private KakaoAccount kakao_account;

        // 현재 이미지는 카카오에서 주고 있는데 여기서 사용하지 않으므로
        // @JsonIgnoreProperties(ignoreUnknown = true) 처리함
        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Properties {
            private String nickname;
//            private String email;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KakaoAccount {
            private String name;
            private String email;
            private Profile profile;

        }
        @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Profile {
                private String nickname;             // profile.nickname
                private String thumbnail_image_url;  // profile.thumbnail_image_url
                private String profile_image_url;    // profile.profile_image_url
            }
        }
    }
