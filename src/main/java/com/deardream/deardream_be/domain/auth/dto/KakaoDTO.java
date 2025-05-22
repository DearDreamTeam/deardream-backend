package com.deardream.deardream_be.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

public class KakaoDTO {

    @Getter
    public static class OAuthToken {
        private String accessToken;
        private String tokenType;
        private String refreshToken;
        private Long expiresIn;
        private String scope;
        private Long refreshExpiresIn;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        private Long id;
        private String connectedAt;
        private Properties properties;
        private KakaoAccount kakaoAccount;

        public String getEmail() {
            return kakaoAccount != null ? kakaoAccount.email : null;
        }

        public String getName() {
            return kakaoAccount != null && kakaoAccount.profile != null
                    ? kakaoAccount.profile.nickname : null;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Properties {
            private String nickname;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class KakaoAccount {
            private String email;
            private Boolean isEmailVerified;
            private Boolean hasEmail;
            private Boolean profileNicknameNeedsAgreement;
            private Boolean emailNeedsAgreement;
            private Boolean isEmailValid;
            private Profile profile;

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public class Profile {
                private String nickname;
                private Boolean isDefaultNickname;
            }
        }
    }
}
