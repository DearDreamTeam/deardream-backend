//package com.deardream.deardream_be.domain.auth.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Getter;
//
//public class KakaoDTO {
//
//    @Getter
//    public static class OAuthToken {
//        private String accessToken;
//        private String tokenType;
//        private String refreshToken;
//        private Long expiresIn;
//        private String scope;
//        private Long refreshExpiresIn;
//    }
//
//    @Getter
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class KakaoProfile {
//        private Long id;
//        private String connectedAt;
//        private Properties properties;
//
//        // 잠시 추가
//        @JsonProperty("kakao_account")
//        private KakaoAccount kakaoAccount;
//
//        public String getEmail() {
//            return kakaoAccount != null ? kakaoAccount.email : null;
//        }
//
//        public String getName() {
//            return kakaoAccount != null && kakaoAccount.profile != null
//                    ? kakaoAccount.profile.nickname : null;
//        }
//
//        @Getter
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        public class Properties {
//            private String nickname;
//        }
//
//        @Getter
//        @JsonIgnoreProperties(ignoreUnknown = true)
//        public class KakaoAccount {
//            @JsonProperty("email")      // 잠시 추가
//            private String email;
//            private Boolean isEmailVerified;
//            private Boolean hasEmail;
//            private Boolean profileNicknameNeedsAgreement;
//            private Boolean emailNeedsAgreement;
//            private Boolean isEmailValid;
//            private Profile profile;
//
//            @Getter
//            @JsonIgnoreProperties(ignoreUnknown = true)
//            public class Profile {
//                private String nickname;
//                private Boolean isDefaultNickname;
//            }
//        }
//    }
//}





package com.deardream.deardream_be.domain.auth.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

public class KakaoDTO {

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
        public class Properties {
            private String nickname;
//            private String email;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean has_email;
            private Boolean profile_nickname_needs_agreement;
            private Boolean email_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public class Profile {
                private String nickname;
                private Boolean is_default_nickname;
            }
        }
    }
}
