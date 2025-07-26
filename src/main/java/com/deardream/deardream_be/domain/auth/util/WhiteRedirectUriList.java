package com.deardream.deardream_be.domain.auth.util;

import java.util.Set;

public class WhiteRedirectUriList {
    private static final Set<String> ALLOWED_REDIRECT_URIS = Set.of(
            "https://www.deardream.site/profile",
            "https://www.deardream.site/admin/login",
            "http://localhost:3000/profile",
            "http://localhost:3000/admin/login",
            "https://deardream-frontend-xi.vercel.app/profile",
            "http://localhost:8080/profile"
    );

    public static Set<String> getAllowedRedirectUris() {
        return ALLOWED_REDIRECT_URIS;
    }
}
