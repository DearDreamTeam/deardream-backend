package com.deardream.deardream_be.domain.payment.util;

import java.util.Set;

public class KakaoPayRedirectUriList {

    private static final Set<String> ALLOWED_REDIRECT_URIS = Set.of(
            "http://localhost:3000",
            "https://www.deardream.site",
            "https://deardream-frontend-xi.vercel.app",
            "http://localhost:8080",
            "https://vote-dream.p-e.kr"

    );

    public static Set<String> getAllowedRedirectUris() {
        return ALLOWED_REDIRECT_URIS;
    }
}
